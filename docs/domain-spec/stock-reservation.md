# 재고 선점(Stock Reservation) 도메인 스펙

## 1. 배경 및 목적

### 현재 방식의 문제

현재 재고 차감은 `PaymentEvent.Success` 이벤트 수신 후에만 일어난다.

```
주문 생성 → 재고 처리 없음 ← ⚠️ 문제 구간
결제 완료 → StockEventListener → StockService.deduct()
```

**문제 1 — 오버셀(Oversell) · Critical**
재고 1개인 상품에 10명이 동시 결제 시:
- 결제는 10명 모두 성공 (돈은 이미 청구)
- `deduct()`는 비관적 락으로 직렬화 → 첫 1명만 성공
- 나머지 9명은 `InsufficientStockException` — 하지만 결제는 완료된 상태
- **결제 됐는데 재고 없음** → 보상(환불) 로직이 전혀 없음

**문제 2 — 차감 실패에 대한 보상 없음**
`StockEventListener`는 `@EventListener`로 예외 발생 시 롤백 없이 로그만 남음.

**문제 3 — 주문 시점에 재고 가시성 없음**
고객은 주문 생성 시 재고 여부를 확인할 수 없음.

### 개선 효과

| Before | After |
|--------|-------|
| 주문 생성 시 재고 미확인 → 결제 후 실패 | 주문 생성 시 선점 → 불가능하면 즉시 실패 |
| 오버셀 가능 | 오버셀 원천 차단 |
| 보상 트랜잭션 필요 | 불필요 (선점 → 확정 흐름) |

---

## 2. 핵심 개념

| 개념 | 정의 |
|------|------|
| **L1 (Physical Stock)** | `stock.quantity` — 실물 재고 |
| **Reserved** | `stock.reserved_quantity` — 주문 생성 시 선점된 수량 |
| **L2 (Available)** | `quantity - reserved_quantity` — 실제 구매 가능 수량 |
| **StockReservation** | 예약 이력 레코드 (PENDING → CONFIRMED / CANCELLED) |

---

## 3. 이벤트 흐름

```
[주문 생성]
order-api: OrderFacade.place()
  → Order 저장 (INIT)
  → POST /internal/v1/stocks/reserve
      → Stock.reserve(qty)         -- reserved_quantity 증가
      → StockReservation 생성 (PENDING)
      → L2 < 0이면 STOCK_INSUFFICIENT → 주문 롤백

[결제 완료]
PaymentEvent.Success → StockEventListener
  → StockService.confirm(orderId)
      → StockReservation.status = CONFIRMED
      → Stock.quantity -= qty      -- L1 실물 차감
      → Stock.reserved_quantity -= qty

[주문 취소]
order-api: 취소 요청
  → POST /internal/v1/stocks/cancel
      → StockReservation.status = CANCELLED
      → Stock.reserved_quantity -= qty  -- L2 복원
```

---

## 4. 데이터 모델

### stock 테이블 변경

```sql
ALTER TABLE stock
  ADD COLUMN reserved_quantity BIGINT NOT NULL DEFAULT 0;
```

| 컬럼 | 설명 |
|------|------|
| `quantity` | L1 실물 재고 |
| `reserved_quantity` | 예약 중인 수량 (신규) |

> `L2 = quantity - reserved_quantity` (가상 계산값, DB 컬럼 아님)

### stock_reservation 테이블 (신규)

```sql
CREATE TABLE stock_reservation (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  ref_stock_id  BIGINT NOT NULL,
  ref_order_id  BIGINT NOT NULL,
  quantity      BIGINT NOT NULL,
  status        ENUM('PENDING','CONFIRMED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  created_at    DATETIME(6) NOT NULL,
  updated_at    DATETIME(6) NOT NULL,
  KEY idx_order_id (ref_order_id),
  KEY idx_stock_id (ref_stock_id)
);
```

| 컬럼 | 설명 |
|------|------|
| `ref_stock_id` | 연결된 재고 ID (stock.id) |
| `ref_order_id` | 연결된 주문 ID |
| `quantity` | 예약 수량 |
| `status` | `PENDING`(주문생성) → `CONFIRMED`(결제완료) / `CANCELLED`(취소) |

---

## 5. API 스펙

### Internal API (commerce-api ← order-api 호출)

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/internal/v1/stocks/reserve` | 주문 생성 시 재고 선점 |
| `POST` | `/internal/v1/stocks/cancel` | 주문 취소 시 선점 해제 |

**예약 요청:**
```json
POST /internal/v1/stocks/reserve
{
  "orderId": 1001,
  "items": [
    { "productId": 10, "quantity": 2 }
  ]
}
```

**예약 성공 응답:**
```json
{
  "meta": { "code": 200, "message": "OK" },
  "data": { "orderId": 1001, "status": "PENDING" }
}
```

**재고 부족 시:**
```json
{
  "meta": { "code": 400, "message": "STOCK_INSUFFICIENT" },
  "data": null
}
```

**취소 요청:**
```json
POST /internal/v1/stocks/cancel
{
  "orderId": 1001
}
```

---

## 6. 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| `STOCK_INSUFFICIENT` | 400 | 가용 재고(L2) 부족 |
| `STOCK_NOT_FOUND` | 404 | 상품의 재고 정보 없음 |
| `RESERVATION_NOT_FOUND` | 404 | 예약 정보 없음 (confirm/cancel 시) |

---

## 7. 동시성 제어

| 작업 | 전략 |
|------|------|
| `reserve()` | `findStocksForUpdate()` 비관적 락 (기존 패턴 유지) |
| `confirm()` | `findByOrderIdForUpdate()` 비관적 락 |
| `cancel()` | `findByOrderIdForUpdate()` 비관적 락 |

---

## 8. 구현 모듈 배치

```
[commerce-api]
domain/stock/
  Stock.java                       -- reservedQuantity 필드, reserve/confirm/cancel 메서드 추가
  StockReservation.java            -- 신규 (예약 엔티티)
  StockReservationRepository.java  -- 신규 인터페이스
  StockService.java                -- reserve/confirm/cancel 메서드 추가, deduct 대체
  StockCommand.java                -- ReserveStocks/ConfirmReservation/CancelReservation 추가
  StockEventListener.java          -- deduct() → confirm() 으로 변경

infrastructure/stock/
  StockReservationJpaRepository.java   -- 신규 (비관적 락 쿼리 포함)
  StockReservationCoreRepository.java  -- 신규 구현체

application/stock/
  StockFacade.java                 -- 신규

interfaces/api/stock/
  StockInternalV1Controller.java   -- 신규 (reserve, cancel)
  StockInternalV1ApiSpec.java      -- 신규
  StockInternalV1Dto.java          -- 신규

[order-api]
infrastructure/client/
  StockApiClient.java              -- 신규 (reserve, cancel HTTP 호출)

application/order/
  OrderFacade.java                 -- 신규 or 수정 (주문 생성 후 reserve 호출)
```

---

## 9. 검증 시나리오

| # | 시나리오 | 기대 결과 |
|---|---------|----------|
| 1 | 재고 10개 → 5개 주문 | reserved=5, L2=5, 주문 성공 |
| 2 | 재고 10개 → 11개 주문 | STOCK_INSUFFICIENT, 주문 롤백 |
| 3 | 재고 10개, 동시 주문 10건 (각 1개) | 비관적 락으로 직렬화 → 10건 모두 성공 |
| 4 | 재고 10개, 동시 주문 11건 (각 1개) | 10건 성공, 1건 STOCK_INSUFFICIENT |
| 5 | 예약 후 결제 완료 | reserved=0, L1=9, L2=9 |
| 6 | 예약 후 주문 취소 | reserved=0, L1=10, L2=10 복원 |

---

## 10. 구현 순서

```
1. DB 스키마 변경 (stock + stock_reservation)
2. 도메인 레이어 (Stock 수정, StockReservation 신규, StockService 수정)
3. 인프라 레이어 (StockReservationJpaRepository, CoreRepository)
4. Application 레이어 (StockFacade)
5. Internal API 레이어 (Controller, ApiSpec, Dto)
6. StockEventListener 수정 (deduct → confirm)
7. order-api 연동 (StockApiClient, OrderFacade)
8. 테스트 작성
```
