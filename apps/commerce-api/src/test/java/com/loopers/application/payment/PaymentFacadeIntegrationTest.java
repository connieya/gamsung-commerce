package com.loopers.application.payment;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.exception.PointException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentFacadeIntegrationTest {

    @Autowired
    PaymentFacade paymentFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("주문에 대한 결제 진행 시 유저 포인트가 차감되고 상품 재고가 차감된 뒤 결제에 성공한다.")
    void pay_success() {
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point initialPoint = Point.create(savedUser.getUserId(), 10_000L);
        pointRepository.save(initialPoint);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 5L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 10L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(1L)
                .build();

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(2L)
                .build();
        OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1, orderItem2), 0L);
        Order order = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(order);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.POINT);

        // when
        PaymentResult paymentResult = paymentFacade.pay(criteria);
        Point updatedPoint = pointRepository.findByUserId("gunny").get();

        // then
        assertAll(
                ()-> assertThat(paymentResult.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETE),
                ()-> assertThat(updatedPoint.getValue()).isEqualTo(5000L)
        );
    }

    @Test
    @DisplayName("재고가 존재하지 않거나 부족할 경우 주문은 실패해야 한다.")
    void pay_throwsException_whenStockIsInsufficient() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point point = Point.create(savedUser.getUserId(), 1000_000L);
        pointRepository.save(point);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 5L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 150L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(2L)
                .build();

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(200L)
                .build();
        OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1, orderItem2), 5000L);
        Order order = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(order);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.POINT);

        // when &  then
        assertThatThrownBy(
                () -> {
                    paymentFacade.pay(criteria);
                }
        ).isInstanceOf(ProductException.InsufficientStockException.class);

    }

    @Test
    @DisplayName("주문 시 유저의 포인트 잔액이 부족할 경우 주문은 실패해야 한다")
    void pay_throwsException_whenPointInsufficientException() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point point = Point.create(savedUser.getUserId(), 5000L);
        pointRepository.save(point);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 5L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 150L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(2L)
                .build();

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(10L)
                .build();
        OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1, orderItem2), 5000L);
        Order order = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(order);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.POINT);

        // when &  then
        assertThatThrownBy(
                () -> {
                    paymentFacade.pay(criteria);
                }
        ).isInstanceOf(PointException.PointInsufficientException.class);

    }

    @Test
    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다. ")
    void pay_handlesConcurrentStockDeductionCorrectly() throws InterruptedException {
        // given
        // 여러 사용자, 여러 주문을 미리 생성
        List<User> users = new ArrayList<>();
        List<Order> orders = new ArrayList<>();


        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 50L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 50L);

        Stock savedStock1 = stockRepository.save(stock1);
        Stock savedStock2 = stockRepository.save(stock2);

        int threadCount = 5;
        for (int i = 0; i < threadCount; i++) {
            // 동시 요청을 보낼 사용자마다 고유한 아이디를 가진 User와 Point 생성
            User user = UserFixture.complete().set(Select.field(User::getUserId), "user" + i).create();
            User savedUser = userRepository.save(user);
            pointRepository.save(Point.create(savedUser.getUserId(), 1000_000L));
            users.add(savedUser);
            // 각 사용자의 주문 생성 (동일 상품에 대한 주문)
            OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                    .productId(savedProduct1.getId())
                    .price(1000L)
                    .quantity(2L)
                    .build();
            OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                    .productId(savedProduct2.getId())
                    .price(2000L)
                    .quantity(2L)
                    .build();

            OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1, orderItem2), 0L);
            orders.add(orderRepository.save(Order.create(orderCommand)));
        }


        // when

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // 각 스레드가 서로 다른 주문을 처리
                    Order orderToPay = orders.get(index);
                    User userPaying = users.get(index);
                    PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(userPaying.getUserId(), orderToPay.getId(), PaymentMethod.POINT);
                    paymentFacade.pay(criteria);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Stock stock = stockRepository.findById(savedStock1.getId()).get();

        assertThat(stock).isNotNull();
        assertThat(stock.getQuantity()).isEqualTo(40L);
    }


    @Test
    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
    void pay_deductsPointsCorrectly_whenSameUserHasConcurrentOrders() throws InterruptedException {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        pointRepository.save(Point.create(savedUser.getUserId(), 10000L));

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 50L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 50L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(1L)
                .build();
        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(2L)
                .build();

        OrderCommand orderCommand1 = OrderCommand.of(savedUser.getId(), List.of(orderItem1), 0L);
        Order savedOrder1 = orderRepository.save(Order.create(orderCommand1));

        OrderCommand orderCommand2 = OrderCommand.of(savedUser.getId(), List.of(orderItem2), 0L);
        Order savedOrder2 = orderRepository.save(Order.create(orderCommand2));


        // when
        PaymentCriteria.Pay criteria1 = new PaymentCriteria.Pay(savedUser.getUserId(), savedOrder1.getId(), PaymentMethod.POINT);
        PaymentCriteria.Pay criteria2 = new PaymentCriteria.Pay(savedUser.getUserId(), savedOrder2.getId(), PaymentMethod.POINT);

        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threadCount);

        executorService.submit(() -> {
            try {
                paymentFacade.pay(criteria1);
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                paymentFacade.pay(criteria2);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        // then
        Point point = pointRepository.findByUserId(savedUser.getUserId()).get();

        assertThat(point).isNotNull();
        assertThat(point.getValue()).isEqualTo(5000L);

    }


    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void pay_throwsOptimisticLockingFailureException_whenSameCouponUsedConcurrently() throws InterruptedException {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        pointRepository.save(Point.create(savedUser.getUserId(), 10000L));

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 50L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 50L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        Coupon savedCoupon = couponRepository.save(Coupon.create("여름 이벤트", CouponType.FIXED_AMOUNT, 1000L));
        userCouponRepository.save(UserCoupon.create(savedUser.getId(), savedCoupon.getId()));

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(1L)
                .build();
        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(2L)
                .build();

        OrderCommand orderCommand1 = OrderCommand.of(savedUser.getId(), List.of(orderItem1), 1000L);
        Order savedOrder1 = orderRepository.save(Order.create(orderCommand1));

        OrderCommand orderCommand2 = OrderCommand.of(savedUser.getId(), List.of(orderItem2), 1000L);
        Order savedOrder2 = orderRepository.save(Order.create(orderCommand2));

        PaymentCriteria.Pay criteria1 = new PaymentCriteria.Pay(savedUser.getUserId(), savedOrder1.getId(), PaymentMethod.POINT);


        PaymentCriteria.Pay criteria2 = new PaymentCriteria.Pay(savedUser.getUserId(), savedOrder2.getId(), PaymentMethod.POINT);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);


        // when
        executorService.submit(() -> {
            try {
                startLatch.await();
                paymentFacade.pay(criteria1);
                successCount.incrementAndGet();
            } catch (ObjectOptimisticLockingFailureException e) {
                // 낙관적 락 충돌 예외를 성공적으로 잡음
                failureCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                startLatch.await();
                paymentFacade.pay(criteria2);
                successCount.incrementAndGet();
            } catch (ObjectOptimisticLockingFailureException e) {
                // 낙관적 락 충돌 예외를 성공적으로 잡음
                failureCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown(); // 모든 스레드에게 동시에 시작 신호를 보냄
        endLatch.await();       // 모든 스레드가 작업을 마칠 때까지 대기

        executorService.shutdown();

        // then
        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

    }

}
