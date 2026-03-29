# LLD: 재고 선점(Stock Reservation)

## 1. DB 스키마

### 1-1. stock 테이블 변경

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
  `reserved_quantity` BIGINT NOT NULL DEFAULT 0,        -- 신규
  PRIMARY KEY (`id`),
  KEY `idx_ref_product_id` (`ref_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 1-2. stock_reservation 테이블 (신규)

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

---

## 2. 도메인 레이어 (commerce-api)

### 2-1. Stock.java 변경

```java
// 기존 필드에 추가
@Column(name = "reserved_quantity", nullable = false)
private Long reservedQuantity = 0L;

// 신규 메서드

/** 주문 생성 시 재고 선점 */
public void reserve(Long quantity) {
    long available = this.quantity - this.reservedQuantity;
    if (available < quantity) {
        throw new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT);
    }
    this.reservedQuantity += quantity;
}

/** 주문 취소 시 선점 해제 */
public void releaseReservation(Long quantity) {
    this.reservedQuantity -= quantity;
}

/** 결제 완료 시 확정 — L1 차감 + 예약 해제 */
public void confirmReservation(Long quantity) {
    this.quantity -= quantity;
    this.reservedQuantity -= quantity;
}

// 기존 deduct() — confirm()으로 대체되므로 삭제 또는 @Deprecated 처리
```

### 2-2. StockReservation.java (신규)

```java
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

    public static StockReservation create(Long stockId, Long orderId, Long quantity) {
        StockReservation r = new StockReservation();
        r.stockId = stockId;
        r.orderId = orderId;
        r.quantity = quantity;
        r.status = ReservationStatus.PENDING;
        return r;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
```

### 2-3. StockReservationRepository.java (신규)

```java
public interface StockReservationRepository {
    StockReservation save(StockReservation reservation);
    List<StockReservation> saveAll(List<StockReservation> reservations);
    List<StockReservation> findByOrderId(Long orderId);
    List<StockReservation> findByOrderIdForUpdate(Long orderId);  // 비관적 락
}
```

### 2-4. StockCommand.java 변경

```java
public class StockCommand {

    // 기존 DeductStocks — 하위 호환을 위해 유지, 내부 사용 제거

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReserveStocks {
        private Long orderId;
        private List<Item> items;

        @Getter
        @Builder
        public static class Item {
            private final Long productId;
            private final Long quantity;
        }

        @Builder
        private ReserveStocks(Long orderId, List<Item> items) {
            this.orderId = orderId;
            this.items = items;
        }

        public static ReserveStocks of(Long orderId, List<Item> items) {
            return ReserveStocks.builder()
                    .orderId(orderId)
                    .items(items)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConfirmReservation {
        private Long orderId;

        @Builder
        private ConfirmReservation(Long orderId) {
            this.orderId = orderId;
        }

        public static ConfirmReservation of(Long orderId) {
            return ConfirmReservation.builder()
                    .orderId(orderId)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CancelReservation {
        private Long orderId;

        @Builder
        private CancelReservation(Long orderId) {
            this.orderId = orderId;
        }

        public static CancelReservation of(Long orderId) {
            return CancelReservation.builder()
                    .orderId(orderId)
                    .build();
        }
    }
}
```

### 2-5. StockService.java 변경

confirm()에서 N+1 문제를 방지하기 위해 stockId 목록을 수집한 뒤 `findStocksForUpdate()`로 배치 조회한다.

```java
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockReservationRepository reservationRepository;

    @Transactional
    public void reserve(StockCommand.ReserveStocks command) {
        List<Long> productIds = command.getItems().stream()
                .map(StockCommand.ReserveStocks.Item::getProductId).toList();
        Map<Long, Long> qtyMap = command.getItems().stream()
                .collect(Collectors.toMap(
                        StockCommand.ReserveStocks.Item::getProductId,
                        StockCommand.ReserveStocks.Item::getQuantity
                ));

        List<Stock> stocks = stockRepository.findStocksForUpdate(productIds);  // 비관적 락

        List<StockReservation> reservations = stocks.stream().map(stock -> {
            Long qty = qtyMap.get(stock.getProductId());
            stock.reserve(qty);                                                 // 재고 부족 시 예외
            return StockReservation.create(stock.getId(), command.getOrderId(), qty);
        }).toList();

        stockRepository.saveAll(stocks);
        reservationRepository.saveAll(reservations);
    }

    @Transactional
    public void confirm(StockCommand.ConfirmReservation command) {
        List<StockReservation> reservations =
                reservationRepository.findByOrderIdForUpdate(command.getOrderId());  // 비관적 락

        // N+1 방지: stockId 목록으로 배치 조회
        List<Long> stockIds = reservations.stream()
                .map(StockReservation::getStockId).toList();
        Map<Long, Stock> stockMap = stockRepository.findStocksForUpdateByIds(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        reservations.forEach(reservation -> {
            Stock stock = Optional.ofNullable(stockMap.get(reservation.getStockId()))
                    .orElseThrow(() -> new CoreException(ErrorType.STOCK_NOT_FOUND));
            reservation.confirm();
            stock.confirmReservation(reservation.getQuantity());
        });

        stockRepository.saveAll(new ArrayList<>(stockMap.values()));
        reservationRepository.saveAll(reservations);
    }

    @Transactional
    public void cancel(StockCommand.CancelReservation command) {
        List<StockReservation> reservations =
                reservationRepository.findByOrderIdForUpdate(command.getOrderId());  // 비관적 락

        List<Long> stockIds = reservations.stream()
                .map(StockReservation::getStockId).toList();
        Map<Long, Stock> stockMap = stockRepository.findStocksForUpdateByIds(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        reservations.forEach(reservation -> {
            Stock stock = Optional.ofNullable(stockMap.get(reservation.getStockId()))
                    .orElseThrow(() -> new CoreException(ErrorType.STOCK_NOT_FOUND));
            reservation.cancel();
            stock.releaseReservation(reservation.getQuantity());
        });

        stockRepository.saveAll(new ArrayList<>(stockMap.values()));
        reservationRepository.saveAll(reservations);
    }
}
```

### 2-6. StockRepository.java 변경

`findStocksForUpdateByIds()` 메서드를 추가한다 (id 기준 배치 비관적 락 조회).

```java
public interface StockRepository {
    // 기존 메서드 유지
    List<Stock> findStocksForUpdate(List<Long> productIds);  // productId 기준
    List<Stock> saveAll(List<Stock> stocks);
    Optional<Stock> findById(Long id);

    // 신규
    List<Stock> findStocksForUpdateByIds(List<Long> ids);   // stockId 기준, confirm/cancel 배치 조회용
}
```

### 2-7. StockEventListener.java 변경

```java
// 변경 후 — deduct() → confirm() 교체
@EventListener
public void onPaymentSuccess(PaymentEvent.Success event) {
    stockService.confirm(StockCommand.ConfirmReservation.of(event.orderId()));
}
```

> `StockEventListener`는 `StockFacade`를 거치지 않고 `StockService`를 직접 호출한다.
> confirm은 외부 API 호출이 아닌 내부 이벤트 기반 처리이므로 Facade 경유가 불필요하다.

---

## 3. 인프라 레이어 (commerce-api)

### 3-1. StockReservationJpaRepository.java (신규)

```java
public interface StockReservationJpaRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM StockReservation r WHERE r.orderId = :orderId")
    List<StockReservation> findByOrderIdForUpdate(@Param("orderId") Long orderId);
}
```

### 3-2. StockReservationCoreRepository.java (신규)

```java
@Repository
@RequiredArgsConstructor
public class StockReservationCoreRepository implements StockReservationRepository {

    private final StockReservationJpaRepository jpaRepository;

    @Override
    public StockReservation save(StockReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public List<StockReservation> saveAll(List<StockReservation> reservations) {
        return jpaRepository.saveAll(reservations);
    }

    @Override
    public List<StockReservation> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<StockReservation> findByOrderIdForUpdate(Long orderId) {
        return jpaRepository.findByOrderIdForUpdate(orderId);
    }
}
```

### 3-3. StockJpaRepository.java 변경 (기존 파일에 추가)

```java
// 기존 findStocksForUpdate (productId 기준) 유지
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Stock s WHERE s.productId IN :productIds")
List<Stock> findStocksForUpdate(@Param("productIds") List<Long> productIds);

// 신규: stockId 기준 배치 조회 (confirm/cancel 용)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Stock s WHERE s.id IN :ids")
List<Stock> findStocksForUpdateByIds(@Param("ids") List<Long> ids);
```

---

## 4. Application 레이어 (commerce-api)

### 4-1. StockFacade.java (신규)

`StockFacade`는 외부 API(Controller) 진입점인 reserve/cancel만 위임한다.
confirm은 내부 이벤트(`StockEventListener`)에서 `StockService`를 직접 호출하므로 Facade를 거치지 않는다.

```java
@Component
@RequiredArgsConstructor
public class StockFacade {

    private final StockService stockService;

    public void reserve(StockCommand.ReserveStocks command) {
        stockService.reserve(command);
    }

    public void cancel(StockCommand.CancelReservation command) {
        stockService.cancel(command);
    }
}
```

---

## 5. API 레이어 (commerce-api)

### 5-1. StockInternalV1Dto.java (신규)

```java
public class StockInternalV1Dto {

    public record ReserveRequest(
            Long orderId,
            List<Item> items
    ) {
        public record Item(Long productId, Long quantity) {}

        public StockCommand.ReserveStocks toCommand() {
            List<StockCommand.ReserveStocks.Item> commandItems = items.stream()
                    .map(i -> StockCommand.ReserveStocks.Item.builder()
                            .productId(i.productId())
                            .quantity(i.quantity())
                            .build())
                    .toList();
            return StockCommand.ReserveStocks.of(orderId, commandItems);
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

### 5-2. StockInternalV1ApiSpec.java (신규)

```java
public interface StockInternalV1ApiSpec {

    @Operation(summary = "재고 선점", description = "주문 생성 시 재고를 선점한다.")
    ApiResponse<StockInternalV1Dto.ReserveResponse> reserve(
            @RequestBody StockInternalV1Dto.ReserveRequest request);

    @Operation(summary = "재고 선점 취소", description = "주문 취소 시 선점된 재고를 해제한다.")
    ApiResponse<Void> cancel(
            @RequestBody StockInternalV1Dto.CancelRequest request);
}
```

### 5-3. StockInternalV1Controller.java (신규)

```java
@RestController
@RequestMapping("/internal/v1/stocks")
@RequiredArgsConstructor
public class StockInternalV1Controller implements StockInternalV1ApiSpec {

    private final StockFacade stockFacade;

    @PostMapping("/reserve")
    public ApiResponse<StockInternalV1Dto.ReserveResponse> reserve(
            @RequestBody StockInternalV1Dto.ReserveRequest request) {
        stockFacade.reserve(request.toCommand());
        return ApiResponse.success(StockInternalV1Dto.ReserveResponse.of(request.orderId()));
    }

    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(
            @RequestBody StockInternalV1Dto.CancelRequest request) {
        stockFacade.cancel(request.toCommand());
        return ApiResponse.success(null);
    }
}
```

**Error Cases:**

| 상황 | HTTP Status | ErrorType |
|------|-------------|-----------|
| 재고 부족 | 400 | `STOCK_INSUFFICIENT` |
| 재고 정보 없음 | 404 | `STOCK_NOT_FOUND` |
| 예약 정보 없음 | 404 | `RESERVATION_NOT_FOUND` |

---

## 6. order-api 연동

order-api는 기존 `CommerceApiClient`(Feign Client)에 stock 엔드포인트 메서드를 추가한다.
별도 `StockApiClient` 클래스를 신규 생성하지 않는다.

### 6-1. CommerceApiClient.java 변경 (메서드 추가)

```java
@FeignClient(name = "commerce-api", url = "${service.commerce-api.url}")
public interface CommerceApiClient {

    // 기존 메서드 유지
    @GetMapping("/internal/v1/users/{userId}")
    ApiResponse<CommerceApiDto.UserResponse> getUser(@PathVariable("userId") String userId);

    @PostMapping("/internal/v1/products/bulk")
    ApiResponse<List<CommerceApiDto.ProductResponse>> getProducts(
            @RequestBody CommerceApiDto.ProductBulkRequest request);

    @PostMapping("/internal/v1/payments/ready")
    ApiResponse<CommerceApiDto.PaymentReadyResponse> paymentReady(
            @RequestBody CommerceApiDto.PaymentReadyRequest request);

    // 신규: 재고 선점
    @PostMapping("/internal/v1/stocks/reserve")
    ApiResponse<CommerceApiDto.StockReserveResponse> reserveStock(
            @RequestBody CommerceApiDto.StockReserveRequest request);

    // 신규: 재고 선점 취소
    @PostMapping("/internal/v1/stocks/cancel")
    ApiResponse<Void> cancelStock(
            @RequestBody CommerceApiDto.StockCancelRequest request);
}
```

### 6-2. CommerceApiDto.java 변경 (record 추가)

```java
public class CommerceApiDto {

    // 기존 record 유지 ...

    // 신규: 재고 선점 요청
    public record StockReserveRequest(
            Long orderId,
            List<StockItem> items
    ) {
        public record StockItem(Long productId, Long quantity) {}
    }

    // 신규: 재고 선점 응답
    public record StockReserveResponse(Long orderId, String status) {}

    // 신규: 재고 선점 취소 요청
    public record StockCancelRequest(Long orderId) {}
}
```

### 6-3. OrderFacade.java 변경

재고 선점은 `getOrCreateOrder()`로 신규 Order가 생성된 직후, `order.validatePay()` 호출 전에 실행한다.
Order가 이미 존재하는 경우(재시도)에는 선점 API를 호출하지 않는다.

```java
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final CommerceApiClient commerceApiClient;
    private final CouponService couponService;
    private final OrderService orderService;
    private final OrderNoIssuer orderNoIssuer;
    private final CartService cartService;

    @Transactional
    public CommerceApiDto.PaymentReadyResponse ready(
            String orderNo, String orderKey, OrderCriteria.Ready criteria) {

        boolean isNewOrder = orderService.findOrderByOrderNumber(orderNo).isEmpty();
        Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());

        // 신규 주문인 경우에만 재고 선점
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

        CommerceApiDto.PaymentReadyRequest readyRequest = new CommerceApiDto.PaymentReadyRequest(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getFinalAmount(),
                criteria.paymentMethod(),
                criteria.payKind(),
                orderKey
        );

        return commerceApiClient.paymentReady(readyRequest).data();
    }

    public void cancel(Long orderId) {
        orderService.cancel(orderId);
        commerceApiClient.cancelStock(new CommerceApiDto.StockCancelRequest(orderId));
    }

    // 기존 getOrCreateOrder(), getOrderDetail(), getOrders(), issueOrderNo(), getOrderForm() 유지
}
```

---

## 7. 에러 타입 추가

### commerce-api ErrorType

```java
// ErrorType enum 또는 상수에 추가
STOCK_INSUFFICIENT(400, "재고가 부족합니다."),
STOCK_NOT_FOUND(404, "재고 정보를 찾을 수 없습니다."),
RESERVATION_NOT_FOUND(404, "예약 정보를 찾을 수 없습니다."),
```

---

## 8. 테스트 전략

### 단위 테스트

**StockTest.java (수정)**
```
- reserve_성공: quantity=10, reserved=0, reserve(5) → reserved=5
- reserve_실패_재고부족: quantity=10, reserved=8, reserve(5) → InsufficientStockException
- releaseReservation_성공: reserved=5, release(5) → reserved=0
- confirmReservation_성공: quantity=10, reserved=3, confirm(3) → quantity=7, reserved=0
```

**StockReservationTest.java (신규)**
```
- create: status=PENDING
- confirm: PENDING → CONFIRMED
- cancel: PENDING → CANCELLED
```

### 서비스 테스트 (Mockito)

**StockServiceTest.java (수정)**
```
- reserve_성공: Mock Stock 반환, reserve() 호출 검증, saveAll 호출 검증
- reserve_실패_재고부족: InsufficientStockException 전파 검증
- confirm_성공: Mock Reservation(PENDING) 반환, confirm() 후 CONFIRMED 검증, N+1 없이 배치 조회 검증
- cancel_성공: Mock Reservation(PENDING) 반환, cancel() 후 CANCELLED 검증
```

### API 테스트 (@WebMvcTest)

**StockInternalV1ControllerTest.java (신규)**
```
- POST /internal/v1/stocks/reserve → 200 OK (AC-01: 재고 선점 성공)
- POST /internal/v1/stocks/reserve (재고 부족) → 400 STOCK_INSUFFICIENT (AC-02: 재고 부족 실패)
- POST /internal/v1/stocks/cancel → 200 OK (AC-05: 취소 시 재고 복원)
```

---

## 9. 구현 파일 목록

### commerce-api — 수정
| 파일 | 변경 내용 |
|------|----------|
| `domain/stock/Stock.java` | `reservedQuantity` 필드, `reserve/releaseReservation/confirmReservation` 메서드 추가 |
| `domain/stock/StockCommand.java` | `ReserveStocks`, `ConfirmReservation`, `CancelReservation` 추가 |
| `domain/stock/StockRepository.java` | `findStocksForUpdateByIds(List<Long> ids)` 메서드 추가 |
| `domain/stock/StockService.java` | `reserve/confirm/cancel` 추가, `deduct` 제거 |
| `domain/stock/StockEventListener.java` | `deduct()` → `confirm()` 교체 |
| `infrastructure/stock/StockJpaRepository.java` | `findStocksForUpdateByIds` 쿼리 메서드 추가 |
| `infrastructure/stock/StockCoreRepository.java` | `findStocksForUpdateByIds` 구현 추가 |
| `src/main/resources/data-local-clean.sql` | `stock` 테이블 `reserved_quantity` 추가, `stock_reservation` 테이블 추가 |

### commerce-api — 신규
| 파일 | 역할 |
|------|------|
| `domain/stock/StockReservation.java` | 예약 엔티티 |
| `domain/stock/StockReservationRepository.java` | 예약 리포지토리 인터페이스 |
| `infrastructure/stock/StockReservationJpaRepository.java` | JPA + 비관적 락 쿼리 |
| `infrastructure/stock/StockReservationCoreRepository.java` | 리포지토리 구현체 |
| `application/stock/StockFacade.java` | 파사드 (reserve, cancel) |
| `interfaces/api/stock/StockInternalV1Controller.java` | Internal API 컨트롤러 |
| `interfaces/api/stock/StockInternalV1ApiSpec.java` | Swagger 스펙 |
| `interfaces/api/stock/StockInternalV1Dto.java` | 요청/응답 DTO |

### order-api — 수정
| 파일 | 변경 내용 |
|------|----------|
| `infrastructure/feign/commerce/CommerceApiClient.java` | `reserveStock`, `cancelStock` 메서드 추가 |
| `infrastructure/feign/commerce/CommerceApiDto.java` | `StockReserveRequest`, `StockReserveResponse`, `StockCancelRequest` record 추가 |
| `application/order/OrderFacade.java` | `ready()` 내 재고 선점 호출, `cancel()` 내 재고 복원 호출 추가 |
