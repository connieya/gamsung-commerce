# HLD: 재고 선점(Stock Reservation)

## 1. 아키텍처 개요

재고 선점은 **commerce-api**(재고 관리)와 **order-api**(주문 관리) 두 모듈에 걸친 변경이다.
order-api가 주문 결제 준비 시점에 commerce-api의 Internal API를 호출하여 재고를 선점하고,
결제 완료·주문 취소 이벤트에 따라 선점 상태를 전이(Confirm / Cancel)한다.

```
┌─────────────────────────────────────────────────────────────────┐
│                          order-api                              │
│                                                                 │
│  OrderFacade.ready()                                            │
│    ├─ getOrCreateOrder()  → Order 저장 (INIT)                  │
│    └─ commerceApiClient.reserveStock(orderId, items) ──────┐    │
│                                                            │    │
│  (주문 취소 시)                                              │    │
│  OrderFacade.cancel()                                      │    │
│    ├─ orderService.cancel(orderId)                         │    │
│    └─ commerceApiClient.cancelStock(orderId) ─────────────┤    │
└────────────────────────────────────────────────────────────┼────┘
                                                             │
                                              POST /internal/v1/stocks/reserve
                                              POST /internal/v1/stocks/cancel
                                                             │
┌────────────────────────────────────────────────────────────┼────┐
│                        commerce-api                        │    │
│                                                            ▼    │
│  StockInternalV1Controller                                       │
│    └─ StockFacade                                               │
│         ├─ reserve()  → StockService.reserve()                  │
│         └─ cancel()   → StockService.cancel()                   │
│                                                                 │
│  StockService                                                   │
│    ├─ reserve()  → Stock.reserve() + StockReservation 생성      │
│    ├─ confirm()  → Stock.confirmReservation() + 예약 확정        │
│    └─ cancel()   → Stock.releaseReservation() + 예약 취소        │
│                                                                 │
│  StockEventListener                                             │
│    └─ onPaymentSuccess(PaymentEvent.Success)                    │
│         └─ StockService.confirm(orderId)   ← 기존 deduct() 대체  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 이벤트 흐름

### 2-1. 주문 결제 준비 (재고 선점)

재고 선점은 `OrderFacade.ready()` 내 `getOrCreateOrder()` 완료 직후에 실행한다.
Order가 신규 생성된 경우에만 선점 API를 호출한다.

```
order-api                              commerce-api
────────────────────────────────────────────────────
OrderFacade.ready(orderNo, orderKey, criteria)
  │
  ├─ getOrCreateOrder()               → Order 저장 (INIT) [신규인 경우]
  │
  └─ commerceApiClient.reserveStock(orderId, items)
       │
       └──► POST /internal/v1/stocks/reserve
              │
              StockFacade.reserve(command)
                └─ StockService.reserve()
                     ├─ findStocksForUpdate(productIds)  ← 비관적 락
                     ├─ stock.reserve(qty)
                     │    └─ (quantity - reserved_quantity) < qty
                     │         → STOCK_INSUFFICIENT 예외
                     │         → order-api 예외 전파 → Order 롤백
                     ├─ StockReservation 생성 (PENDING)
                     └─ 저장
```

### 2-2. 결제 완료 (예약 확정)

```
commerce-api 내부
────────────────────────────────────
PaymentEvent.Success 발행
  │
  └─► StockEventListener.onPaymentSuccess()
        └─ StockService.confirm(orderId)       ← 기존 deduct() 대체
             ├─ findByOrderIdForUpdate()        ← 비관적 락
             ├─ findStocksForUpdate(stockIds)   ← 배치 조회, 비관적 락
             ├─ reservation.confirm()           → status=CONFIRMED
             ├─ stock.confirmReservation(qty)
             │    ├─ quantity -= qty            (L1 실물 차감)
             │    └─ reserved_quantity -= qty   (예약 해제)
             └─ 저장
```

### 2-3. 주문 취소 (예약 해제)

```
order-api                              commerce-api
────────────────────────────────────────────────────
OrderFacade.cancel(orderId)
  │
  ├─ orderService.cancel(orderId)     → Order status=CANCELLED
  │
  └─ commerceApiClient.cancelStock(orderId)
       │
       └──► POST /internal/v1/stocks/cancel
              │
              StockFacade.cancel(command)
                └─ StockService.cancel()
                     ├─ findByOrderIdForUpdate()  ← 비관적 락
                     ├─ reservation.cancel()      → status=CANCELLED
                     ├─ stock.releaseReservation(qty)
                     │    └─ reserved_quantity -= qty  (L2 복원)
                     └─ 저장
```

---

## 3. 재고 상태 모델

### Stock 수치 관계

```
L1 (quantity)          = 창고 실물 재고
Reserved               = 주문 생성 시 선점된 수량 (reserved_quantity)
L2 (available)         = quantity - reserved_quantity  ← 고객이 구매 가능한 수량

이벤트별 변화:
  입고          : quantity += n
  주문 생성      : reserved_quantity += n  (L2 감소)
  결제 완료      : quantity -= n, reserved_quantity -= n  (L1 감소, L2 유지)
  주문 취소      : reserved_quantity -= n  (L2 복원)
```

### StockReservation 상태 전이

```
       주문 생성
          │
          ▼
       PENDING
       /       \
결제 완료      주문 취소
      │              │
      ▼              ▼
  CONFIRMED      CANCELLED
```

---

## 4. 데이터 모델 개요

### 변경 테이블

| 테이블 | 변경 내용 |
|--------|----------|
| `stock` | `reserved_quantity BIGINT NOT NULL DEFAULT 0` 컬럼 추가 |

### 신규 테이블

| 테이블 | 목적 |
|--------|------|
| `stock_reservation` | 예약 이력 관리 (주문별 선점 수량 + 상태) |

---

## 5. 컴포넌트 역할

### commerce-api

| 레이어 | 컴포넌트 | 역할 |
|--------|---------|------|
| interfaces/api | `StockInternalV1Controller` | Internal API 진입점 (reserve, cancel) |
| application | `StockFacade` | 오케스트레이션 (reserve, cancel 위임) |
| domain | `StockService` | reserve / confirm / cancel 비즈니스 로직 |
| domain | `Stock` | L1/Reserved 수치 관리 도메인 엔티티 |
| domain | `StockReservation` | 예약 이력 엔티티 (상태 전이) |
| domain | `StockEventListener` | PaymentEvent.Success → StockService.confirm 호출 |

> `StockEventListener`는 `StockService`를 직접 호출한다. `StockFacade`를 경유하지 않는다.
> confirm은 외부 API가 아닌 내부 이벤트 기반이므로 Controller/Facade 레이어를 거치지 않는다.

### order-api

| 레이어 | 컴포넌트 | 역할 |
|--------|---------|------|
| application | `OrderFacade` | 주문 생성/취소 + commerce-api 재고 선점 오케스트레이션 |
| infrastructure | `CommerceApiClient` | commerce-api Feign 클라이언트 (stock 엔드포인트 추가) |
| infrastructure | `CommerceApiDto` | 요청/응답 DTO (StockReserve, StockCancel 추가) |

> order-api는 기존 `CommerceApiClient`(Feign)에 stock 엔드포인트 메서드를 추가하는 방식으로 구현한다.
> 별도 `StockApiClient`를 신규 생성하지 않는다.

---

## 6. 동시성 전략

| 작업 | 전략 | 이유 |
|------|------|------|
| reserve | `SELECT ... FOR UPDATE` (비관적 락, Stock 기준) | 동시 주문 시 재고 수치 정합성 보장 |
| confirm | `SELECT ... FOR UPDATE` (예약 기준 + Stock 배치) | 중복 confirm 방지, N+1 방지 |
| cancel | `SELECT ... FOR UPDATE` (예약 기준) | 중복 cancel 방지 |

> 기존 `findStocksForUpdate()` PESSIMISTIC_WRITE 패턴을 그대로 재사용.

---

## 7. 장애 시나리오

| 시나리오 | 처리 방식 |
|---------|----------|
| reserve 호출 중 commerce-api 장애 | Feign 예외 전파 → `OrderFacade.ready()` 트랜잭션 롤백 → Order 미생성 |
| confirm 중 예외 발생 | `@EventListener` 예외 → 결제는 완료, 재고 수동 조정 필요 (추후 Dead Letter Queue 도입) |
| cancel 중 예외 발생 | Feign 예외 → `OrderFacade.cancel()` 예외 전파, 재시도 or 수동 처리 |

> confirm 실패 처리는 이번 범위 밖. 추후 이벤트 아웃박스 패턴으로 보완 예정.
