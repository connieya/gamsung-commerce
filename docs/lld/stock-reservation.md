# LLD: 재고 선점(Stock Reservation)

## 도메인 모델

### [LLD-ENTITY-01] Stock

`stock` 테이블에 `reserved_quantity` 필드를 추가하고, 재고 선점/해제/확정 메서드를 추가한다.

```java
// apps/commerce-api/src/main/java/com/loopers/domain/stock/Stock.java
@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Column(name = "ref_sku_id", nullable = true)
    private Long skuId;

    private Long quantity;

    // [LLD-ENTITY-01] 신규 필드 — 주문 생성 시 선점된 수량
    @Column(name = "reserved_quantity", nullable = false)
    private Long reservedQuantity = 0L;

    @Builder
    private Stock(Long productId, Long quantity) { ... }

    public static Stock create(Long productId, Long quantity) { ... }

    // 기존 — confirm()으로 대체되어 @Deprecated 처리 또는 제거 대상
    public void deduct(Long quantity) { ... }

    // [LLD-ENTITY-01] 주문 생성 시 재고 선점: available < quantity이면 STOCK_INSUFFICIENT
    public void reserve(Long quantity) {
        long available = this.quantity - this.reservedQuantity;
        if (available < quantity) {
            throw new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT);
        }
        this.reservedQuantity += quantity;
    }

    // [LLD-ENTITY-01] 주문 취소 시 선점 해제
    public void releaseReservation(Long quantity) {
        this.reservedQuantity -= quantity;
    }

    // [LLD-ENTITY-01] 결제 완료 시 L1 차감 + 예약 해제
    public void confirmReservation(Long quantity) {
        this.quantity -= quantity;
        this.reservedQuantity -= quantity;
    }
}
```

**수치 관계 요약**

| 이벤트 | quantity | reserved_quantity | available (계산값) |
|--------|----------|------------------|-------------------|
| 입고 | +n | 변화 없음 | +n |
| 주문 생성 (reserve) | 변화 없음 | +n | -n |
| 결제 완료 (confirm) | -n | -n | 변화 없음 |
| 주문 취소 (cancel) | 변화 없음 | -n | +n |

---

### [LLD-ENTITY-02] StockReservation (신규)

주문별 재고 선점 이력을 관리하는 엔티티. `stock_reservation` 테이블과 매핑된다.

```java
// apps/commerce-api/src/main/java/com/loopers/domain/stock/StockReservation.java
@Entity
@Table(name = "stock_reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservation extends BaseEntity {

    @Column(name = "ref_stock_id", nullable = false)
    private Long stockId;

    @Column(name = "ref_order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    public enum ReservationStatus {
        PENDING, CONFIRMED, CANCELLED
    }

    // [LLD-ENTITY-02] 팩토리 메서드 — 최초 생성 시 status = PENDING
    public static StockReservation create(Long stockId, Long orderId, Long quantity) {
        StockReservation r = new StockReservation();
        r.stockId = stockId;
        r.orderId = orderId;
        r.quantity = quantity;
        r.status = ReservationStatus.PENDING;
        return r;
    }

    // [LLD-ENTITY-02] 결제 완료 시 PENDING → CONFIRMED
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    // [LLD-ENTITY-02] 주문 취소 시 PENDING → CANCELLED
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
```

---

## API 스펙

### [LLD-API-01] 재고 선점 — POST /internal/v1/stocks/reserve

| 항목 | 값 |
|------|-----|
| Method | POST |
| Path | `/internal/v1/stocks/reserve` |
| 인증 | 불필요 (Internal API, 내부망 전용) |
| 연결 AC | AC-01, AC-02, AC-03 |

**Request:**
```json
{
  "orderId": 1001,
  "items": [
    { "productId": 10, "quantity": 5 },
    { "productId": 11, "quantity": 2 }
  ]
}
```

**Response (200 OK):**
```json
{
  "meta": { "code": "SUCCESS" },
  "data": {
    "orderId": 1001,
    "status": "PENDING"
  }
}
```

**Error Cases:**

| 상황 | HTTP Status | 에러 코드 | AC 연결 |
|------|-------------|-----------|---------|
| 가용 재고 부족 (available < 요청 수량) | 400 | `STOCK_INSUFFICIENT` | AC-02, AC-03 |
| 재고 정보 없음 (productId에 해당하는 stock 없음) | 404 | `STOCK_NOT_FOUND` | - |
| 요청 필드 유효성 위반 | 400 | `BAD_REQUEST` | - |

---

### [LLD-API-02] 재고 선점 취소 — POST /internal/v1/stocks/cancel

| 항목 | 값 |
|------|-----|
| Method | POST |
| Path | `/internal/v1/stocks/cancel` |
| 인증 | 불필요 (Internal API, 내부망 전용) |
| 연결 AC | AC-05 |

**Request:**
```json
{
  "orderId": 1001
}
```

**Response (200 OK):**
```json
{
  "meta": { "code": "SUCCESS" },
  "data": null
}
```

**Error Cases:**

| 상황 | HTTP Status | 에러 코드 | AC 연결 |
|------|-------------|-----------|---------|
| 해당 orderId의 예약 정보 없음 | 404 | `RESERVATION_NOT_FOUND` | - |
| 재고 정보 없음 (stockId에 해당하는 stock 없음) | 404 | `STOCK_NOT_FOUND` | - |
| 요청 필드 유효성 위반 | 400 | `BAD_REQUEST` | - |

---

## DB 스키마

### [LLD-DB-01] stock 테이블 변경

기존 `stock` 테이블에 `reserved_quantity` 컬럼을 추가한다.

```sql
ALTER TABLE stock
  ADD COLUMN reserved_quantity BIGINT NOT NULL DEFAULT 0;
```

**변경 후 전체 스키마:**
```sql
CREATE TABLE `stock` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `created_at`        DATETIME(6) NOT NULL,
  `updated_at`        DATETIME(6) NOT NULL,
  `deleted_at`        DATETIME(6) DEFAULT NULL,
  `ref_product_id`    BIGINT NOT NULL,
  `ref_sku_id`        BIGINT DEFAULT NULL,
  `quantity`          BIGINT DEFAULT NULL,
  `reserved_quantity` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_ref_product_id` (`ref_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### [LLD-DB-02] stock_reservation 테이블 신규 생성

```sql
CREATE TABLE `stock_reservation` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `created_at`    DATETIME(6) NOT NULL,
  `updated_at`    DATETIME(6) NOT NULL,
  `ref_stock_id`  BIGINT NOT NULL,
  `ref_order_id`  BIGINT NOT NULL,
  `quantity`      BIGINT NOT NULL,
  `status`        ENUM('PENDING','CONFIRMED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`ref_order_id`),
  KEY `idx_stock_id` (`ref_stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> `deleted_at` 컬럼 없음 — 예약은 논리 삭제가 아닌 `status` 변경으로 상태를 관리한다.
> `idx_order_id`: `findByOrderId` / `findByOrderIdForUpdate` 조회 최적화.
> `idx_stock_id`: 재고별 예약 집계 조회 최적화 (현재 미사용, 향후 확장 고려).

---

## 주요 메서드 시그니처

### Controller — [LLD-API-03]

```java
// StockInternalV1Controller
@PostMapping("/reserve")
public ApiResponse<StockInternalV1Dto.ReserveResponse> reserve(
        @RequestBody StockInternalV1Dto.ReserveRequest request);

@PostMapping("/cancel")
public ApiResponse<Void> cancel(
        @RequestBody StockInternalV1Dto.CancelRequest request);
```

### Facade — [LLD-FACADE-01]

```java
// StockFacade
public void reserve(StockCommand.ReserveStocks command);

public void cancel(StockCommand.CancelReservation command);
```

> confirm은 `StockEventListener`가 `StockService`를 직접 호출하므로 Facade에 포함하지 않는다.

### Service — [LLD-SERVICE-01]

```java
// StockService
@Transactional
public void reserve(StockCommand.ReserveStocks command);

@Transactional
public void confirm(StockCommand.ConfirmReservation command);

@Transactional
public void cancel(StockCommand.CancelReservation command);
```

### Repository — [LLD-REPO-01]

```java
// StockRepository (도메인 인터페이스)
List<Stock> findStocksForUpdate(List<Long> productIds);           // 기존 — productId 기준 비관적 락
List<Stock> findStocksForUpdateByIds(List<Long> ids);             // 신규 — stockId 기준 비관적 락 (confirm/cancel 배치)
List<Stock> saveAll(List<Stock> stocks);
Optional<Stock> findById(Long id);

// StockReservationRepository (신규 도메인 인터페이스)
StockReservation save(StockReservation reservation);
List<StockReservation> saveAll(List<StockReservation> reservations);
List<StockReservation> findByOrderId(Long orderId);
List<StockReservation> findByOrderIdForUpdate(Long orderId);      // 비관적 락
```

### JPA Repository — [LLD-REPO-02]

```java
// StockJpaRepository (추가 메서드)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Stock s WHERE s.id IN :ids")
List<Stock> findStocksForUpdateByIds(@Param("ids") List<Long> ids);

// StockReservationJpaRepository (신규)
List<StockReservation> findByOrderId(Long orderId);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM StockReservation r WHERE r.orderId = :orderId")
List<StockReservation> findByOrderIdForUpdate(@Param("orderId") Long orderId);
```

### EventListener — [LLD-EVENT-01]

```java
// StockEventListener (deduct → confirm 교체)
@EventListener
public void onPaymentSuccess(PaymentEvent.Success event) {
    stockService.confirm(StockCommand.ConfirmReservation.of(event.orderId()));
}
```

### StockCommand — [LLD-SERVICE-02]

```java
public class StockCommand {

    @Getter
    @Builder
    public static class ReserveStocks {
        private final Long orderId;
        private final List<Item> items;

        @Getter
        @Builder
        public static class Item {
            private final Long productId;
            private final Long quantity;
        }
    }

    @Getter
    @Builder
    public static class ConfirmReservation {
        private final Long orderId;

        public static ConfirmReservation of(Long orderId) {
            return ConfirmReservation.builder().orderId(orderId).build();
        }
    }

    @Getter
    @Builder
    public static class CancelReservation {
        private final Long orderId;

        public static CancelReservation of(Long orderId) {
            return CancelReservation.builder().orderId(orderId).build();
        }
    }
}
```

### DTO — [LLD-API-04]

```java
// StockInternalV1Dto
public class StockInternalV1Dto {

    public record ReserveRequest(Long orderId, List<Item> items) {
        public record Item(Long productId, Long quantity) {}

        public StockCommand.ReserveStocks toCommand() {
            List<StockCommand.ReserveStocks.Item> commandItems = items.stream()
                    .map(i -> StockCommand.ReserveStocks.Item.builder()
                            .productId(i.productId())
                            .quantity(i.quantity())
                            .build())
                    .toList();
            return StockCommand.ReserveStocks.builder()
                    .orderId(orderId)
                    .items(commandItems)
                    .build();
        }
    }

    public record CancelRequest(Long orderId) {
        public StockCommand.CancelReservation toCommand() {
            return StockCommand.CancelReservation.of(orderId);
        }
    }

    public record ReserveResponse(Long orderId, String status) {
        public static ReserveResponse of(Long orderId) {
            return new ReserveResponse(orderId, "PENDING");
        }
    }
}
```

### order-api CommerceApiClient — [LLD-FACADE-02]

```java
// CommerceApiClient (Feign — 기존 파일에 메서드 추가)
@PostMapping("/internal/v1/stocks/reserve")
ApiResponse<CommerceApiDto.StockReserveResponse> reserveStock(
        @RequestBody CommerceApiDto.StockReserveRequest request);

@PostMapping("/internal/v1/stocks/cancel")
ApiResponse<Void> cancelStock(
        @RequestBody CommerceApiDto.StockCancelRequest request);
```

### order-api CommerceApiDto — [LLD-FACADE-03]

```java
// CommerceApiDto (기존 파일에 record 추가)
public record StockReserveRequest(Long orderId, List<StockItem> items) {
    public record StockItem(Long productId, Long quantity) {}
}

public record StockReserveResponse(Long orderId, String status) {}

public record StockCancelRequest(Long orderId) {}
```

### order-api OrderFacade — [LLD-FACADE-04]

```java
// OrderFacade.ready() — 신규 주문 생성 시에만 재고 선점 호출
@Transactional
public CommerceApiDto.PaymentReadyResponse ready(
        String orderNo, String orderKey, OrderCriteria.Ready criteria) {

    boolean isNewOrder = orderService.findOrderByOrderNumber(orderNo).isEmpty();
    Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());

    if (isNewOrder) {
        List<CommerceApiDto.StockReserveRequest.StockItem> stockItems =
                criteria.orderItems().stream()
                        .map(item -> new CommerceApiDto.StockReserveRequest.StockItem(
                                item.productId(), item.quantity()))
                        .toList();
        commerceApiClient.reserveStock(
                new CommerceApiDto.StockReserveRequest(order.getId(), stockItems));
    }

    order.validatePay();
    // ... paymentReady 호출
}

// OrderFacade.cancel() — 주문 취소 시 재고 선점 해제 호출
public void cancel(Long orderId) {
    orderService.cancel(orderId);
    commerceApiClient.cancelStock(new CommerceApiDto.StockCancelRequest(orderId));
}
```

---

## 유효성 검증 규칙

<!-- [LLD-VALID-01] -->

### POST /internal/v1/stocks/reserve

| 필드 | 규칙 | 위반 시 에러 코드 |
|------|------|-----------------|
| `orderId` | Not null, 양의 정수 | `BAD_REQUEST` |
| `items` | Not null, 1개 이상 | `BAD_REQUEST` |
| `items[*].productId` | Not null, 양의 정수 | `BAD_REQUEST` |
| `items[*].quantity` | Not null, 1 이상 | `BAD_REQUEST` |
| `items[*].productId` → stock 존재 여부 | `StockRepository`에 해당 productId의 stock이 존재해야 함 | `STOCK_NOT_FOUND` |
| 가용 재고 | `stock.quantity - stock.reserved_quantity >= items[*].quantity` | `STOCK_INSUFFICIENT` |

### POST /internal/v1/stocks/cancel

| 필드 | 규칙 | 위반 시 에러 코드 |
|------|------|-----------------|
| `orderId` | Not null, 양의 정수 | `BAD_REQUEST` |
| `orderId` → 예약 존재 여부 | `StockReservationRepository`에 해당 orderId의 PENDING 예약이 존재해야 함 | `RESERVATION_NOT_FOUND` |

---

## 예외 처리

<!-- [LLD-ERR-01] -->

| 상황 | 예외 타입 | HTTP Status | 에러 코드 | AC 연결 |
|------|-----------|-------------|-----------|---------|
| 가용 재고 부족 (`available < quantity`) | `ProductException.InsufficientStockException` | 400 | `STOCK_INSUFFICIENT` | AC-02, AC-03 |
| 재고 선점 후 결제 완료로 예약 확정 성공 | — (정상 흐름) | — | — | AC-04 |
| 주문 취소로 선점 해제 성공 | — (정상 흐름) | — | — | AC-05 |
| productId에 해당하는 stock 없음 | `CoreException` | 404 | `STOCK_NOT_FOUND` | - |
| orderId에 해당하는 예약 없음 | `CoreException` | 404 | `RESERVATION_NOT_FOUND` | - |
| stockId에 해당하는 stock 없음 (confirm/cancel 내부) | `CoreException` | 404 | `STOCK_NOT_FOUND` | - |
| 요청 필드 유효성 위반 | `MethodArgumentNotValidException` 또는 `CoreException` | 400 | `BAD_REQUEST` | - |
| reserve 중 commerce-api 장애 (Feign 예외) | Feign 예외 전파 → `OrderFacade.ready()` 트랜잭션 롤백 | 5xx | — | - |

### [LLD-ERR-02] ErrorType 추가 (commerce-api)

```java
// ErrorType enum에 추가
STOCK_INSUFFICIENT(400, "재고가 부족합니다."),
STOCK_NOT_FOUND(404, "재고 정보를 찾을 수 없습니다."),
RESERVATION_NOT_FOUND(404, "예약 정보를 찾을 수 없습니다."),
```

---

## AC 연결 현황

| PRD AC ID | LLD 반영 여부 | 반영 위치 |
|-----------|-------------|---------|
| AC-01 (재고 선점 성공) | O | API 스펙 POST /reserve Response, 유효성 검증 |
| AC-02 (재고 부족 시 주문 실패) | O | Error Cases STOCK_INSUFFICIENT, 예외 처리 |
| AC-03 (동시 주문 오버셀 방지) | O | Error Cases STOCK_INSUFFICIENT, DB 스키마 PESSIMISTIC_WRITE |
| AC-04 (결제 완료 시 예약 확정) | O | EventListener [LLD-EVENT-01], StockReservation.confirm() [LLD-ENTITY-02] |
| AC-05 (주문 취소 시 재고 복원) | O | API 스펙 POST /cancel, StockReservation.cancel() [LLD-ENTITY-02] |

---

## 구현 파일 목록

### commerce-api — 수정

| 파일 | 변경 내용 |
|------|----------|
| `domain/stock/Stock.java` | `reservedQuantity` 필드, `reserve/releaseReservation/confirmReservation` 메서드 추가, `deduct` 제거 또는 Deprecated |
| `domain/stock/StockCommand.java` | `ReserveStocks`, `ConfirmReservation`, `CancelReservation` 추가 |
| `domain/stock/StockRepository.java` | `findStocksForUpdateByIds(List<Long> ids)` 추가 |
| `domain/stock/StockService.java` | `reserve/confirm/cancel` 추가 |
| `domain/stock/StockEventListener.java` | `deduct()` → `confirm()` 교체 |
| `infrastructure/stock/StockJpaRepository.java` | `findStocksForUpdateByIds` 쿼리 메서드 추가 |
| `infrastructure/stock/StockCoreRepository.java` | `findStocksForUpdateByIds` 구현 추가 |
| `src/main/resources/data-local-clean.sql` | `stock` 테이블 `reserved_quantity` 추가, `stock_reservation` 테이블 생성 |

### commerce-api — 신규

| 파일 | 역할 |
|------|------|
| `domain/stock/StockReservation.java` | 예약 엔티티 [LLD-ENTITY-02] |
| `domain/stock/StockReservationRepository.java` | 예약 리포지토리 인터페이스 [LLD-REPO-01] |
| `infrastructure/stock/StockReservationJpaRepository.java` | JPA + 비관적 락 쿼리 [LLD-REPO-02] |
| `infrastructure/stock/StockReservationCoreRepository.java` | 리포지토리 구현체 |
| `application/stock/StockFacade.java` | reserve/cancel 위임 [LLD-FACADE-01] |
| `interfaces/api/stock/StockInternalV1Controller.java` | Internal API 컨트롤러 [LLD-API-03] |
| `interfaces/api/stock/StockInternalV1ApiSpec.java` | Swagger 스펙 |
| `interfaces/api/stock/StockInternalV1Dto.java` | 요청/응답 DTO [LLD-API-04] |

### order-api — 수정

| 파일 | 변경 내용 |
|------|----------|
| `infrastructure/feign/commerce/CommerceApiClient.java` | `reserveStock`, `cancelStock` 메서드 추가 [LLD-FACADE-02] |
| `infrastructure/feign/commerce/CommerceApiDto.java` | `StockReserveRequest`, `StockReserveResponse`, `StockCancelRequest` 추가 [LLD-FACADE-03] |
| `application/order/OrderFacade.java` | `ready()` 재고 선점 호출, `cancel()` 재고 복원 호출 추가 [LLD-FACADE-04] |
