package com.loopers.interfaces.api.payment;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.point.PointEntity;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RequiredArgsConstructor
@SprintE2ETest
public class PaymentV1ApiE2ETest {

    private static final String BASE_URL = "/api/v1/payments";

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("포인트 결제 요청 시 결제에 성공하고, PAID 상태를 반환한다.")
    void pay_returnsPaidStatus_whenUsingPoint() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

        Brand brand = Brand.create("nike", "just do it!");
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

        Point point = Point.create("gunny", 100000L);
        PointEntity pointEntity = PointEntity.from(point);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(pointEntity));

        Product product = ProductFixture.complete()
                .set(Select.field(Product::getName), "foo")
                .set(Select.field(Product::getPrice), 5000L)
                .create();
        ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

        OrderCommand.OrderItem orderItem = OrderCommand.OrderItem.builder()
                .productId(product.getId())
                .price(product.getPrice())
                .quantity(2L)
                .build();
        OrderCommand orderCommand = OrderCommand.builder()
                .userId(userEntity.getId())
                .orderItems(List.of(orderItem))
                .discountAmount(0L)
                .build();
        Order order = Order.create(orderCommand);

        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(order));


        PaymentV1Dto.Request.Pay requestBody = new PaymentV1Dto.Request.Pay(order.getId(), PaymentMethod.POINT, CardType.HYUNDAI, "1234-5678-9012-3456");

        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.USER_ID, "gunny");

        // when
        ResponseEntity<ApiResponse<PaymentV1Dto.Response.Pay>> response = testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), new ParameterizedTypeReference<>() {
        });

        // then
        assertAll(
                () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(),
                () -> assertThat(response.getBody().data().paymentStatus()).isEqualTo(PaymentStatus.PAID)
        );

    }


    @Test
    @DisplayName("카드 결제 요청 시 PG사 처리를 위해 PENDING 상태를 반환한다.")
        // FIXME PG Simulator 호출을 위한 임시 테스트
    void pay_returnsPendingStatus_whenUsingCard() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

        Brand brand = Brand.create("nike", "just do it!");
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

        Product product = ProductFixture.complete()
                .set(Select.field(Product::getName), "foo")
                .set(Select.field(Product::getPrice), 5000L)
                .create();
        ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

        OrderCommand.OrderItem orderItem = OrderCommand.OrderItem.builder()
                .productId(product.getId())
                .price(product.getPrice())
                .quantity(2L)
                .build();
        OrderCommand orderCommand = OrderCommand.builder()
                .userId(userEntity.getId())
                .orderItems(List.of(orderItem))
                .discountAmount(0L)
                .build();
        Order order = Order.create(orderCommand);

        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(order));


        PaymentV1Dto.Request.Pay requestBody = new PaymentV1Dto.Request.Pay(order.getId(), PaymentMethod.CARD, CardType.HYUNDAI, "1234-5678-9012-3456");

        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.USER_ID, "gunny");

        // when
        ResponseEntity<ApiResponse<PaymentV1Dto.Response.Pay>> response = testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), new ParameterizedTypeReference<>() {
        });

        // then
        assertAll(
                () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(),
                () -> assertThat(response.getBody().data().paymentStatus()).isEqualTo(PaymentStatus.PENDING)
        );

    }


}
