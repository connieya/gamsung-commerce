# Stock Ledger System (SLS) PRD

## 1. 개요 (Overview)

### 목적
SKU 단위의 정확한 재고 추적을 위해 L1(실물 재고)과 L2(가용 재고)를 분리 관리하는 원장 시스템 구축.

### 배경
| 문제 | 현황 | 개선 방향 |
|------|------|----------|
| SKU 단위 재고 없음 | `stock` 테이블이 Product 레벨만 관리, `ref_sku_id` 미사용 | SKU(옵션 조합) 단위 재고 필수화 |
| 오버셀(Oversell) 위험 | 주문 생성-결제 사이 재고 선점 없음 | 주문 생성 시 예약(Reserve) 도입 |
| 입고 API 없음 | pbo-api에 창고·입고 관리 부재 | Warehouse 도메인 + 입고 API 신규 구현 |

---

## 2. 핵심 개념

| 개념 | 정의 |
|------|------|
| **L1 (Physical Stock)** | 창고에 실제 존재하는 실물 재고. 입고(Inbound)로 증가, 배송 확정(Fulfillment)으로 감소 |
| **L2 (Available Stock)** | 실제 판매 가능한 재고 = L1 - Reserved. 고객이 조회하는 재고 수치 |
| **Reserved** | 주문 생성 시 L1에서 선점한 수량. 결제 완료 시 확정, 취소 시 복원 |
| **Inbound** | 파트너가 창고에 상품을 입고하는 행위. L1 증가 원인 |
| **SKU 단위 재고** | 옵션 조합(색상+사이즈) 단위로 구분되는 재고 |

---

## 3. L1 / L2 공식

```
L2(가용 재고) = L1(실물 재고) - Reserved(예약 수량)

L1 증가: 입고(Inbound)
L1 감소: 배송 확정(Fulfillment) → 결제 완료 + 출고 처리
Reserved 증가: 주문 생성 (Order Created)
Reserved 감소: 결제 완료(Payment Confirmed) or 주문 취소(Order Cancelled)
```

---

## 4. 이벤트 흐름

```
[입고]
pbo-api: POST /api/v1/warehouses/{warehouseId}/inbound
  → stock.quantity (L1) += inboundQuantity (SKU 단위)

[주문 생성]
order-api: 주문 생성 시 commerce-api Internal API 호출
  POST /internal/v1/stocks/reserve
  → StockReservation 생성 (status=PENDING)
  → stock.reserved_quantity += quantity
  → L2 = L1 - reserved_quantity < 0이면 STOCK_INSUFFICIENT 예외

[결제 완료]
PaymentEvent.Success (Kafka)
  → StockReservation.status = CONFIRMED
  → stock.quantity (L1) -= confirmedQuantity
  → stock.reserved_quantity -= confirmedQuantity

[주문 취소]
OrderCancelEvent (Kafka)
  → StockReservation.status = CANCELLED
  → stock.reserved_quantity -= cancelledQuantity (L2 복원)

[상품 조회]
commerce-api: GET /api/v1/skus/{skuId}/stock
  → L2 = stock.quantity - stock.reserved_quantity 계산 후 반환
```

---

## 5. 데이터 모델

### 5-1. stock 테이블 변경 (SKU 연결 + 예약 수량 추가)

```sql
-- 기존 stock 테이블 변경
ALTER TABLE stock
  MODIFY COLUMN ref_sku_id BIGINT NOT NULL,                       -- nullable → NOT NULL (SKU 필수화)
  ADD COLUMN reserved_quantity BIGINT NOT NULL DEFAULT 0;         -- 예약 수량 컬럼 추가
```

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | 재고 원장 ID |
| ref_product_id | BIGINT NOT NULL | 상품 ID |
| ref_sku_id | BIGINT NOT NULL | SKU ID (색상+사이즈 조합 단위) |
| quantity | BIGINT NOT NULL | L1 실물 재고 |
| reserved_quantity | BIGINT NOT NULL DEFAULT 0 | 예약 중인 수량 |
| created_at / updated_at | DATETIME(6) | BaseEntity |

> `available_quantity (L2) = quantity - reserved_quantity` — 가상 계산 컬럼 (DB 컬럼 아님)

### 5-2. stock_reservation 테이블 (신규)

```sql
CREATE TABLE stock_reservation (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  stock_id     BIGINT NOT NULL,
  order_id     BIGINT NOT NULL,
  sku_id       BIGINT NOT NULL,
  quantity     BIGINT NOT NULL,
  status       ENUM('PENDING', 'CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
  created_at   DATETIME(6) NOT NULL,
  updated_at   DATETIME(6) NOT NULL,
  KEY idx_order_id (order_id),
  KEY idx_stock_id (stock_id)
);
```

| 컬럼 | 설명 |
|------|------|
| stock_id | 연결된 재고 원장 ID (stock.id FK) |
| order_id | 연결된 주문 ID |
| sku_id | 예약된 SKU ID |
| quantity | 예약 수량 |
| status | `PENDING`(주문생성) → `CONFIRMED`(결제완료) / `CANCELLED`(취소) |

### 5-3. warehouse 테이블 (신규, pbo-api)

```sql
CREATE TABLE warehouse (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  location    VARCHAR(255),
  created_at  DATETIME(6) NOT NULL,
  updated_at  DATETIME(6) NOT NULL
);
```

| 컬럼 | 설명 |
|------|------|
| name | 창고 이름 |
| location | 창고 위치 주소 |

---

## 6. API 스펙

### pbo-api (파트너 전용)

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/api/v1/warehouses` | 창고 등록 |
| `POST` | `/api/v1/warehouses/{warehouseId}/inbound` | 입고 처리 (L1 증가) |
| `GET`  | `/api/v1/warehouses/{warehouseId}/stocks` | 창고별 재고 현황 |

**창고 등록 요청:**
```json
POST /api/v1/warehouses
{
  "name": "경기 물류센터",
  "location": "경기도 이천시 마장면"
}
```

**입고 요청:**
```json
POST /api/v1/warehouses/1/inbound
{
  "items": [
    { "skuId": 10, "quantity": 100 },
    { "skuId": 11, "quantity": 50 }
  ]
}
```

**입고 응답:**
```json
{
  "meta": { "code": 200, "message": "OK" },
  "data": {
    "warehouseId": 1,
    "processedItems": [
      { "skuId": 10, "addedQuantity": 100, "totalL1": 100 },
      { "skuId": 11, "addedQuantity": 50, "totalL1": 50 }
    ]
  }
}
```

**창고별 재고 현황 응답:**
```json
{
  "meta": { "code": 200, "message": "OK" },
  "data": {
    "warehouseId": 1,
    "stocks": [
      {
        "skuId": 10,
        "l1Quantity": 100,
        "reservedQuantity": 10,
        "l2Quantity": 90
      }
    ]
  }
}
```

---

### commerce-api (고객 전용)

| Method | Path | 설명 |
|--------|------|------|
| `GET`  | `/api/v1/skus/{skuId}/stock` | SKU별 L2(가용 재고) 조회 |

**응답:**
```json
{
  "meta": { "code": 200, "message": "OK" },
  "data": {
    "skuId": 10,
    "availableQuantity": 85,
    "isSoldOut": false
  }
}
```

---

### commerce-api Internal (서비스 간 통신)

| Method | Path | 호출 주체 | 설명 |
|--------|------|----------|------|
| `POST` | `/internal/v1/stocks/reserve` | order-api | 주문 생성 시 재고 예약 |
| `POST` | `/internal/v1/stocks/confirm` | PaymentEvent 컨슈머 | 예약 확정 → L1 차감 |
| `POST` | `/internal/v1/stocks/cancel` | order-api | 주문 취소 시 예약 해제 |

**예약 요청:**
```json
POST /internal/v1/stocks/reserve
{
  "orderId": 1001,
  "items": [
    { "skuId": 10, "quantity": 2 }
  ]
}
```

**예약 응답 (품절 시 에러):**
```json
{
  "meta": { "code": 400, "message": "STOCK_INSUFFICIENT" },
  "data": null
}
```

**예약 확정 요청:**
```json
POST /internal/v1/stocks/confirm
{
  "orderId": 1001
}
```

**예약 취소 요청:**
```json
POST /internal/v1/stocks/cancel
{
  "orderId": 1001
}
```

---

## 7. 구현 모듈 배치

```
[pbo-api]
domain/warehouse/
  Warehouse.java                    # 창고 엔티티
  WarehouseRepository.java          # 인터페이스
  WarehouseService.java             # 입고 처리 포함
  WarehouseCommand.java             # 창고 등록 / 입고 커맨드

domain/stock/ (pbo-api 신규)
  StockLedger.java                  # stock 테이블 (L1)
  StockLedgerRepository.java        # 인터페이스
  StockLedgerService.java           # inbound 처리

application/warehouse/
  WarehouseFacade.java              # 입고 처리 오케스트레이션

interfaces/api/warehouse/
  WarehouseV1Controller.java
  WarehouseV1ApiSpec.java
  WarehouseV1Dto.java

---

[commerce-api]
domain/stock/
  Stock.java                        # L1 + reserved_quantity 추가
  StockReservation.java             # 신규 (예약 엔티티)
  StockRepository.java              # 기존 인터페이스 유지
  StockReservationRepository.java   # 신규 인터페이스
  StockService.java                 # reserve / confirm / cancel 메서드 추가

infrastructure/stock/
  StockCoreRepository.java          # 기존 JPA 구현체 유지
  StockReservationJpaRepository.java  # 신규

application/stock/
  StockFacade.java                  # reserve / confirm / cancel 오케스트레이션

interfaces/api/stock/
  StockInternalV1Controller.java    # /internal/v1/stocks/*
  StockV1Controller.java            # /api/v1/skus/{skuId}/stock
  StockV1Dto.java
```

---

## 8. 동시성 제어

| 작업 | 전략 |
|------|------|
| 예약(Reserve) | SKU별 `SELECT ... FOR UPDATE` 비관적 락 |
| 입고(Inbound) | pbo-api 단일 진입점, 직렬화 처리 가능 |
| 결제 확정(Confirm) | `PaymentEvent.Success` Kafka 이벤트 기반, 단건 처리 |
| 취소(Cancel) | `OrderCancelEvent` Kafka 이벤트 기반, 단건 처리 |

> 동시 예약 시 `reserved_quantity + requestedQuantity > quantity`이면 `STOCK_INSUFFICIENT` 예외 반환.

---

## 9. 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| `STOCK_INSUFFICIENT` | 400 | 가용 재고 부족 |
| `STOCK_NOT_FOUND` | 404 | SKU에 대한 재고 정보 없음 |
| `RESERVATION_NOT_FOUND` | 404 | 예약 정보 없음 |
| `WAREHOUSE_NOT_FOUND` | 404 | 창고 정보 없음 |
| `INVALID_INBOUND_QUANTITY` | 400 | 입고 수량 0 이하 |

---

## 10. 마이그레이션 계획

| Phase | 작업 | 범위 |
|-------|------|------|
| Phase 1 | `stock` 테이블 `ref_sku_id NOT NULL` 전환 + `reserved_quantity` 컬럼 추가 | DB 마이그레이션 |
| Phase 2 | `stock_reservation` 테이블 생성 + Reserve/Confirm/Cancel Internal API 구현 | commerce-api |
| Phase 3 | `warehouse` 테이블 + pbo-api 입고 API 구현 | pbo-api |
| Phase 4 | 기존 Product 레벨 재고 → SKU 레벨 마이그레이션 스크립트 | 데이터 마이그레이션 |

---

## 11. 검증 시나리오

| # | 시나리오 | 기대 결과 |
|---|---------|----------|
| 1 | 입고 100개 → L1=100, L2=100 | L1과 L2 모두 100 |
| 2 | 주문 10개 예약 → Reserved=10 | L1=100, Reserved=10, L2=90 |
| 3 | 결제 완료 → L1 차감 | L1=90, Reserved=0, L2=90 |
| 4 | 주문 취소 → Reserved 복원 | L1=100, Reserved=0, L2=100 |
| 5 | 재고 10개에 11개 주문 | `STOCK_INSUFFICIENT` 에러 반환 |
| 6 | 동시 주문 10개 (재고 10개) | 비관적 락으로 1개만 성공, 나머지 실패 |
| 7 | SKU가 없는 재고 조회 | `STOCK_NOT_FOUND` 에러 반환 |
| 8 | 입고 수량 0 이하 | `INVALID_INBOUND_QUANTITY` 에러 반환 |
