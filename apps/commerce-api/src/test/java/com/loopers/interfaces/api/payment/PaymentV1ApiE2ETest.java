package com.loopers.interfaces.api.payment;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.category.Category;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.infrastructure.point.PointEntity;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@SprintE2ETest
public class PaymentV1ApiE2ETest {

    private static final String BASE_URL = "/api/v1/payments";

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @MockitoBean
    private OrderApiClient orderApiClient;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("포인트 결제 요청 시 결제에 성공하고, PAID 상태를 반환한다.")
    void pay_returnsPaidStatus_whenUsingPoint() throws InterruptedException {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

        Brand brand = Brand.create("nike", "just do it!");
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

        Category category = Category.createRoot("상의", 1);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(category));

        Point point = Point.create("gunny", 100000L);
        PointEntity pointEntity = PointEntity.from(point);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(pointEntity));

        Product product = Product.create("foo", 5000L, brand, category.getId(), null, ZonedDateTime.now());
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

        Coupon coupon = Coupon.create("쿠폰1", "E2E-COUPON-1", CouponType.PERCENTAGE, 10L,
                ZonedDateTime.now().minusDays(30), ZonedDateTime.now().plusDays(30), null);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(coupon));

        UserCoupon userCoupon = UserCoupon.create(userEntity.getId(), coupon.getId(),
                ZonedDateTime.now().plusDays(30));
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userCoupon));

        // Mock: OrderApiClient 응답
        Long orderId = 1L;
        String orderNumber = "ORD-E2E-001";
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                orderId, orderNumber, 10000L, 0L, 10000L, "INIT", userEntity.getId(),
                List.of(new OrderApiDto.OrderLineResponse(product.getId(), 2L, 5000L))
        );
        when(orderApiClient.getOrder(orderId)).thenReturn(ApiResponse.success(orderResponse));

        PaymentV1Dto.Request.Pay requestBody = new PaymentV1Dto.Request.Pay(orderId, PaymentMethod.POINT, PayKind.POINT, CardType.HYUNDAI, "1234-5678-9012-3456", 1L);

        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.USER_ID, "gunny");

        // when
        ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), new ParameterizedTypeReference<>() {
        });

        Thread.sleep(2000);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue()
        );

        transactionTemplate.executeWithoutResult(status -> {
            Payment payment = testEntityManager.getEntityManager()
                    .createQuery("select p from Payment p where p.orderNumber = :orderNumber", Payment.class)
                    .setParameter("orderNumber", orderNumber)
                    .getSingleResult();
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        });
    }

    @Disabled
    @Test
    @DisplayName("카드 결제 요청 시 PG사 처리를 위해 PENDING 상태를 반환한다.")
    void pay_returnsPendingStatus_whenUsingCard() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

        Brand brand = Brand.create("nike", "just do it!");
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

        Category category = Category.createRoot("상의", 1);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(category));

        Product product = Product.create("foo", 5000L, brand, category.getId(), null, ZonedDateTime.now());
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

        // Mock: OrderApiClient 응답
        Long orderId = 1L;
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                orderId, "ORD-E2E-002", 10000L, 0L, 10000L, "INIT", userEntity.getId(),
                List.of(new OrderApiDto.OrderLineResponse(product.getId(), 2L, 5000L))
        );
        when(orderApiClient.getOrder(orderId)).thenReturn(ApiResponse.success(orderResponse));

        PaymentV1Dto.Request.Pay requestBody = new PaymentV1Dto.Request.Pay(orderId, PaymentMethod.CARD, PayKind.CARD, CardType.HYUNDAI, "1234-5678-9012-3456", 1L);

        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.USER_ID, "gunny");

        // when
        ResponseEntity<ApiResponse<PaymentV1Dto.Response.Pay>> response = testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), new ParameterizedTypeReference<>() {
        });

        // then
        assertAll(
                () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue()
        );
    }
}
