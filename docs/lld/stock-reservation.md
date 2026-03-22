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

    // 기존 DeductStocks — 삭제 또는 유지(하위 호환)

    @Getter
    public static class ReserveStocks {
        private final Long orderId;
        private final List<Item> items;

        @Getter
        public static class Item {
            private final Long productId;
            private final Long quantity;
        }

        public static ReserveStocks of(Long orderId, List<Item> items) { ... }
    }

    @Getter
    public static class ConfirmReservation {
        private final Long orderId;
        public static ConfirmReservation of(Long orderId) { ... }
    }

    @Getter
    public static class CancelReservation {
        private final Long orderId;
        public static CancelReservation of(Long orderId) { ... }
    }
}
```

### 2-5. StockService.java 변경

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
                .collect(Collectors.toMap(Item::getProductId, Item::getQuantity));

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

        reservations.forEach(reservation -> {
            Stock stock = stockRepository.findById(reservation.getStockId())
                    .orElseThrow(() -> new CoreException(ErrorType.STOCK_NOT_FOUND));
            reservation.confirm();
            stock.confirmReservation(reservation.getQuantity());
            stockRepository.save(stock);
        });
        reservationRepository.saveAll(reservations);
    }

    @Transactional
    public void cancel(StockCommand.CancelReservation command) {
        List<StockReservation> reservations =
                reservationRepository.findByOrderIdForUpdate(command.getOrderId());  // 비관적 락

        reservations.forEach(reservation -> {
            Stock stock = stockRepository.findById(reservation.getStockId())
                    .orElseThrow(() -> new CoreException(ErrorType.STOCK_NOT_FOUND));
            reservation.cancel();
            stock.releaseReservation(reservation.getQuantity());
            stockRepository.save(stock);
        });
        reservationRepository.saveAll(reservations);
    }
}
```

### 2-6. StockEventListener.java 변경

```java
// 기존
@EventListener
public void onPaymentSuccess(PaymentEvent.Success event) {
    // deduct() 호출 → 삭제
}

// 변경 후
@EventListener
public void onPaymentSuccess(PaymentEvent.Success event) {
    stockService.confirm(StockCommand.ConfirmReservation.of(event.orderId()));
}
```

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

---

## 4. Application 레이어 (commerce-api)

### 4-1. StockFacade.java (신규)

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
                    .map(i -> StockCommand.ReserveStocks.Item.of(i.productId(), i.quantity()))
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

---

## 6. order-api 연동

### 6-1. StockApiClient.java (신규)

```java
@Component
@RequiredArgsConstructor
public class StockApiClient {

    private final RestClient restClient;  // 기존 HTTP 클라이언트 패턴 참고

    public void reserve(Long orderId, List<OrderCommand.OrderItem> items) {
        // POST /internal/v1/stocks/reserve
        // 실패 시 예외 전파 → OrderFacade 트랜잭션 롤백
    }

    public void cancel(Long orderId) {
        // POST /internal/v1/stocks/cancel
    }
}
```

### 6-2. OrderFacade.java (신규 or 수정)

```java
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final StockApiClient stockApiClient;

    @Transactional
    public OrderInfo place(OrderCommand command) {
        OrderInfo orderInfo = orderService.place(command);         // Order 저장
        stockApiClient.reserve(orderInfo.orderId(), command.getOrderItems()); // 재고 선점
        return orderInfo;
        // reserve 실패 시 예외 전파 → @Transactional 롤백 → Order도 롤백
    }

    public void cancel(Long orderId) {
        // 주문 취소 처리 후 재고 복원
        stockApiClient.cancel(orderId);
    }
}
```

---

## 7. 에러 타입 추가

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
- confirm_성공: Mock Reservation(PENDING) 반환, confirm() 후 CONFIRMED 검증
- cancel_성공: Mock Reservation(PENDING) 반환, cancel() 후 CANCELLED 검증
```

### API 테스트 (@WebMvcTest)

**StockInternalV1ControllerTest.java (신규)**
```
- POST /internal/v1/stocks/reserve → 200 OK
- POST /internal/v1/stocks/reserve (재고 부족) → 400 STOCK_INSUFFICIENT
- POST /internal/v1/stocks/cancel → 200 OK
```

---

## 9. 구현 파일 목록

### commerce-api — 수정
| 파일 | 변경 내용 |
|------|----------|
| `domain/stock/Stock.java` | `reservedQuantity` 필드, `reserve/releaseReservation/confirmReservation` 메서드 추가 |
| `domain/stock/StockCommand.java` | `ReserveStocks`, `ConfirmReservation`, `CancelReservation` 추가 |
| `domain/stock/StockService.java` | `reserve/confirm/cancel` 추가, `deduct` 제거 |
| `domain/stock/StockEventListener.java` | `deduct()` → `confirm()` 교체 |
| `src/main/resources/data-local-clean.sql` | `stock` 테이블 `reserved_quantity` 추가, `stock_reservation` 테이블 추가 |

### commerce-api — 신규
| 파일 | 역할 |
|------|------|
| `domain/stock/StockReservation.java` | 예약 엔티티 |
| `domain/stock/StockReservationRepository.java` | 예약 리포지토리 인터페이스 |
| `infrastructure/stock/StockReservationJpaRepository.java` | JPA + 비관적 락 쿼리 |
| `infrastructure/stock/StockReservationCoreRepository.java` | 리포지토리 구현체 |
| `application/stock/StockFacade.java` | 파사드 |
| `interfaces/api/stock/StockInternalV1Controller.java` | Internal API 컨트롤러 |
| `interfaces/api/stock/StockInternalV1ApiSpec.java` | Swagger 스펙 |
| `interfaces/api/stock/StockInternalV1Dto.java` | 요청/응답 DTO |

### order-api — 신규/수정
| 파일 | 역할 |
|------|------|
| `infrastructure/client/StockApiClient.java` | commerce-api HTTP 클라이언트 |
| `application/order/OrderFacade.java` | 주문 생성/취소 + 재고 선점 오케스트레이션 |
