package com.loopers.application.order;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.Category;
import com.loopers.domain.coupon.*;
import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired
    OrderFacade orderFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    CategoryJpaRepository categoryJpaRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void cleanUp() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("사용 불가능하거나 존재하지 않는 쿠폰일 경우 주문은 실패해야 한다.")
    void placeOrder_throwsException_whenUserCouponIsInvalid() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gh").create();
        userRepository.save(user);

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().name("foo1").brand(savedBrand).categoryId(category.getId()).build();
        Product product2 = ProductFixture.create().name("foo2").brand(savedBrand).categoryId(category.getId()).build();

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);


        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct1.getId())
                .quantity(3L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct2.getId())
                .quantity(2L)
                .build();

        Coupon savedCoupon = couponRepository.save(Coupon.create("이벤트", CouponType.PERCENTAGE, 10L));

        OrderCriteria orderCriteria = OrderCriteria.builder()
                .userId("gh")
                .orderItems(List.of(orderItem1, orderItem2))
                .couponId(savedCoupon.getId())
                .build();


        // when & then
        assertThatThrownBy( ()-> {
            orderFacade.place(orderCriteria);
        }).isInstanceOf(CouponException.UserCouponNotFoundException.class);
    }

    @Test
    @DisplayName("주문 성공: 쿠폰 할인이 정상 적용되어 총액 및 할인 금액이 올바르게 계산된다.")
    void placeOrder_success_withCouponApplied()  {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().name("foo1").price(3000L).brand(savedBrand).categoryId(category.getId()).build();
        Product product2 = ProductFixture.create().name("foo2").price(2000L).brand(savedBrand).categoryId(category.getId()).build();

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);


        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct1.getId())
                .quantity(5L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct2.getId())
                .quantity(1L)
                .build();

        Coupon savedCoupon = couponRepository.save(Coupon.create("이벤트", CouponType.PERCENTAGE, 10L));
        userCouponRepository.save(UserCoupon.create(savedUser.getId(), savedCoupon.getId()));

        OrderCriteria orderCriteria = OrderCriteria.builder()
                .userId("gunny")
                .orderItems(List.of(orderItem1, orderItem2))
                .couponId(savedCoupon.getId())
                .build();


        // when
        OrderResult.Create orderResult = orderFacade.place(orderCriteria);


        // then
        assertAll(
                ()-> assertThat(orderResult.getTotalAmount()).isEqualTo(17000L),
                ()-> assertThat(orderResult.getDiscountAmount()).isEqualTo(1700L)

        );
    }

    @Test
    @DisplayName("주문 성공: 쿠폰 미사용 시, 할인 금액이 0원으로 올바르게 계산된다.")
    void placeOrder_success_withoutCouponApplied() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        userRepository.save(user);

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().name("foo1").price(3000L).brand(savedBrand).categoryId(category.getId()).build();
        Product product2 = ProductFixture.create().name("foo2").price(2000L).brand(savedBrand).categoryId(category.getId()).build();

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);


        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct1.getId())
                .quantity(5L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct2.getId())
                .quantity(1L)
                .build();


        OrderCriteria orderCriteria = OrderCriteria.builder()
                .userId("gunny")
                .orderItems(List.of(orderItem1, orderItem2))
                .couponId(0L)
                .build();


        // when
        OrderResult.Create orderResult = orderFacade.place(orderCriteria);


        // then
        assertAll(
                ()-> assertThat(orderResult.getTotalAmount()).isEqualTo(17000L),
                ()-> assertThat(orderResult.getDiscountAmount()).isEqualTo(0L)

        );
    }


    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void order_throwsOptimisticLockingFailureException_whenSameCouponUsedConcurrently() throws InterruptedException {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().price(1000L).brand(savedBrand).categoryId(category.getId()).build();
        Product product2 = ProductFixture.create().price(2000L).brand(savedBrand).categoryId(category.getId()).build();

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);


        Coupon savedCoupon = couponRepository.save(Coupon.create("여름 이벤트", CouponType.FIXED_AMOUNT, 1000L));
        userCouponRepository.save(UserCoupon.create(savedUser.getId(), savedCoupon.getId()));

        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem.builder()
                .productId(savedProduct1.getId())
                .quantity(1L)
                .build();
        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem.builder()
                .productId(savedProduct2.getId())
                .quantity(2L)
                .build();


        OrderCriteria orderCriteria = OrderCriteria.builder()
                .userId("gunny")
                .orderItems(List.of(orderItem1, orderItem2))
                .couponId(savedCoupon.getId())
                .build();

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
                orderFacade.place(orderCriteria);
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
                orderFacade.place(orderCriteria);
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
