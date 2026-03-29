# PRD: 재고 선점(Stock Reservation)

## 1. 개요

### 배경
현재 이커머스 시스템은 주문 생성 시점에 재고를 확인하거나 선점하지 않는다.
재고 차감은 오직 결제 완료(`PaymentEvent.Success`) 이후에만 발생하며, 이로 인해 다음과 같은 심각한 문제가 발생한다.

### 문제 정의

| 문제 | 영향 | 심각도 |
|------|------|--------|
| 주문-결제 사이 재고 선점 없음 → 동시 주문 시 오버셀(Oversell) | 결제 완료 후 재고 없음 → 강제 환불 | Critical |
| 결제 후 재고 차감 실패 시 보상 로직 없음 | 고객 불만, 수동 처리 필요 | High |
| 주문 취소 시 재고 복원 로직 없음 | 재고 수치 불일치 | High |

**구체적 오버셀 시나리오**
```
재고: 1개
사용자 A, B 동시 주문 → 결제 진행
  → A 결제 성공, B 결제 성공 (둘 다 결제 완료)
  → StockService.deduct() 실행:
      A: quantity 1 → 0 (성공)
      B: InsufficientStockException (실패) ← 하지만 결제는 이미 완료
결과: B는 돈은 나갔지만 상품을 받을 수 없음
```

### 목적
주문 생성 시점에 재고를 선점(Reserve)하여 결제 전 재고 가용성을 보장하고, 오버셀을 원천 차단한다.

---

## 2. 목표 (Goals)

- **주문 생성 시 재고 선점** — 가용 재고 부족 시 주문 즉시 실패 처리
- **오버셀 0건** — 결제 완료 후 재고 부족 상황 제거
- **결제 완료 시 예약 확정** — 선점 수량을 실물 재고에서 차감
- **주문 취소 시 재고 복원** — 선점 수량을 가용 재고로 자동 반환

## 비목표 (Non-Goals)

- SKU 단위 재고 관리 (SLS 전체 범위에서 처리)
- 입고(Inbound) API (SLS 범위)
- 재고 조회 Public API

---

## 3. 사용자 스토리

| ID | As | I want to | So that |
|----|-----|-----------|---------|
| US-01 | 고객 | 주문 시 재고 가용 여부를 즉시 알고 싶다 | 결제 후 환불을 당하지 않는다 |
| US-02 | 시스템 | 주문 생성 시 재고를 선점하고 싶다 | 동시 주문으로 인한 오버셀을 방지한다 |
| US-03 | 시스템 | 결제 완료 시 선점된 재고를 확정하고 싶다 | 실물 재고 수치가 정확하게 유지된다 |
| US-04 | 시스템 | 주문 취소 시 선점 재고를 복원하고 싶다 | 취소된 주문의 재고가 다시 판매 가능해진다 |

---

## 4. 인수 조건 (Acceptance Criteria)

### AC-01: 재고 선점 성공
```
Given  재고 10개인 상품
When   5개 주문 생성
Then   주문 성공, reserved_quantity=5, available=5
```

### AC-02: 재고 부족 시 주문 실패
```
Given  재고 10개인 상품
When   11개 주문 생성
Then   STOCK_INSUFFICIENT 오류, 주문 생성 롤백
```

### AC-03: 동시 주문 — 오버셀 방지
```
Given  재고 10개인 상품
When   11명이 동시에 각 1개씩 주문
Then   10명 성공, 1명 STOCK_INSUFFICIENT (오버셀 0건)
```

### AC-04: 결제 완료 시 예약 확정
```
Given  재고 10개, 예약 3개(PENDING)
When   해당 주문 결제 완료
Then   StockReservation.status=CONFIRMED, quantity=7, reserved_quantity=0
```

### AC-05: 주문 취소 시 재고 복원
```
Given  재고 10개, 예약 3개(PENDING)
When   해당 주문 취소
Then   StockReservation.status=CANCELLED, reserved_quantity=0, available=10 복원
```

---

## 5. 이해관계자

| 역할 | 관심사 |
|------|--------|
| 고객 | 주문 시 정확한 재고 정보, 결제 후 환불 없음 |
| 운영팀 | 재고 수치 정확성, 오버셀 모니터링 |
| 개발팀 | 기존 결제 흐름과의 호환성, 동시성 안전성 |

---

## 6. 제약 조건

- 기존 `PaymentEvent.Success` 기반 이벤트 흐름 유지
- order-api ↔ commerce-api 간 HTTP Internal API로 통신 (기존 패턴 준수)
- Product 레벨 재고 유지 (SKU 레벨 전환은 SLS에서)
- 비관적 락 전략 유지 (기존 `findStocksForUpdate` 패턴)

---

## 7. 성공 지표

| 지표 | 목표 |
|------|------|
| 오버셀 발생 건수 | 0건 |
| 주문 생성 시 재고 확인 | 100% |
| 재고-예약 수치 정합성 | 100% |
