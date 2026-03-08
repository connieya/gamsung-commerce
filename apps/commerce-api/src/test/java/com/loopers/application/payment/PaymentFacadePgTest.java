package com.loopers.application.payment;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.Category;
import com.loopers.domain.coupon.*;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.interfaces.api.ApiResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
    PaymentRepository paymentRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    CategoryJpaRepository categoryJpaRepository;

    @MockitoBean
    OrderApiClient orderApiClient;

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

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().price(1000L).brand(savedBrand).categoryId(category.getId()).build();
        Product product2 = ProductFixture.create().price(2000L).brand(savedBrand).categoryId(category.getId()).build();
        productRepository.save(product1);
        productRepository.save(product2);

        Coupon coupon = Coupon.create("쿠폰1", "PG-COUPON-1", CouponType.PERCENTAGE, 10L,
                java.time.ZonedDateTime.now().minusDays(30), java.time.ZonedDateTime.now().plusDays(30), null);
        Coupon savedCoupon = couponRepository.save(coupon);

        UserCoupon userCoupon = UserCoupon.create(savedUser.getId(), savedCoupon.getId(),
                java.time.ZonedDateTime.now().plusDays(30));
        userCouponRepository.save(userCoupon);

        // Mock: OrderApiClient
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                1L, "ORD-PG-001", 5000L, 0L, 5000L, "INIT", savedUser.getId(),
                List.of(new OrderApiDto.OrderLineResponse(1L, 1L, 1000L), new OrderApiDto.OrderLineResponse(2L, 2L, 2000L))
        );
        when(orderApiClient.getOrder(1L)).thenReturn(ApiResponse.success(orderResponse));

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", 1L, PaymentMethod.CARD, PayKind.CARD, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);

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
        Payment payment = paymentRepository.findByOrderNumber("ORD-PG-001").get();

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

        // Mock: OrderApiClient
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                1L, "ORD-PG-002", 5000L, 0L, 5000L, "INIT", savedUser.getId(),
                List.of(new OrderApiDto.OrderLineResponse(1L, 1L, 5000L))
        );
        when(orderApiClient.getOrder(1L)).thenReturn(ApiResponse.success(orderResponse));

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", 1L, PaymentMethod.CARD, PayKind.CARD, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);

        mockServer.stubFor(post("/api/v1/payments")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"meta\": {\"result\": \"SUCCESS\"}}")
                        .withFixedDelay(4000)
                ));

        // when
        paymentFacade.pay(criteria);
        Payment payment = paymentRepository.findByOrderNumber("ORD-PG-002").get();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getAmount()).isEqualTo(5000L);
    }

    @TestConfiguration
    static class FeignTestConfig {
        @Bean("testFeignOptions")
        public Request.Options feignOptions() {
            return new Request.Options(1000, 2000);
        }
    }
}
