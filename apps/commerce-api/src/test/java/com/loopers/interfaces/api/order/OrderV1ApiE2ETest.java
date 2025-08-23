package com.loopers.interfaces.api.order;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.junit.jupiter.api.Assertions.*;

@SprintE2ETest
@RequiredArgsConstructor
class OrderV1ApiE2ETest {

    private static final String BASE_URL = "/api/v1/orders";

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("POST " + BASE_URL)
    @Nested
    class PlaceOrder {


        @DisplayName("주문 요청이 성공적으로 처리되어야 한다.")
        @Test
        void placeOrderSuccess() {
            // given
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product = ProductFixture.complete()
                    .set(Select.field(Product::getName) ,"foo")
                    .set(Select.field(Product::getPrice) ,5000L)
                    .create();
            ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            // when
            OrderV1Dto.Request.Place requestBody = new OrderV1Dto.Request.Place(
                    1L, // couponId
                    List.of(new OrderV1Dto.OrderItem(productEntity.getId(), 2L)) // orderItems
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, "gunny");

            ResponseEntity<ApiResponse<OrderV1Dto.Response.Place>> response = testRestTemplate.exchange(BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), new ParameterizedTypeReference<>() {
            });

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(10000L),
                    () -> assertThat(response.getBody().data().discountAmount()).isEqualTo(0L)
            );
        }

    }


}
