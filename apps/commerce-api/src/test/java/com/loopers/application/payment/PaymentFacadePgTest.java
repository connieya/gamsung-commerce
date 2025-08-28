package com.loopers.application.payment;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import feign.Request;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentFacadePgTest {

    private static final WireMockServer mockServer = new WireMockServer(8082);

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
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    void setUp() {
        mockServer.start();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        mockServer.stop();
    }

    @Test
    @DisplayName("결제 성공 시, 결제 상태는 PAID가 되고 결제 금액이 정상 반영된다.")
    void payment_succeeds_andReturnsPaidStatus() {
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
        Order initialOrder = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(initialOrder);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.CARD, CardType.HYUNDAI, "1234-1234-1234-1234",1L);

        Coupon coupon = Coupon.create("쿠폰1", CouponType.PERCENTAGE, 10L);
        Coupon savedCoupon = couponRepository.save(coupon);

        UserCoupon userCoupon = UserCoupon.create(savedUser.getId(), savedCoupon.getId());
        userCouponRepository.save(userCoupon);


        String body = """
                {
                  "data": {
                    "transactionKey": "tx_2025_0001",
                    "status": "SUCCESS",
                    "reason": null
                  }
                }
                """;


        mockServer.stubFor(post("/api/v1/payments")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withFixedDelay(1000)
                ));


        // when
        paymentFacade.pay(criteria);
        Payment payment = paymentRepository.findByOrderNumber(savedOrder.getOrderNumber()).get();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getAmount()).isEqualTo(5000L);

    }


    @Test
    @DisplayName("PG 타임아웃 발생 시, 결제 상태는 FAILED가 되고 적절한 예외를 던진다.")
    void payment_failsWithTimeout_andReturnsFailedStatus() {
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
        Order initialOrder = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(initialOrder);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.CARD, CardType.HYUNDAI, "1234-1234-1234-1234",1L);
        // when
        mockServer.stubFor(post("/api/v1/payments")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"meta\": {\"result\": \"SUCCESS\"}}")
                        .withFixedDelay(4000)
                ));


        // then
        paymentFacade.pay(criteria);
        Payment payment = paymentRepository.findByOrderNumber(savedOrder.getOrderNumber()).get();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getAmount()).isEqualTo(5000L);

    }

    @TestConfiguration
    static class FeignTestConfig {
        @Bean("testFeignOptions")
        public Request.Options feignOptions() {
            return new Request.Options(
                    1000,
                    2000
            );
        }
    }
}
