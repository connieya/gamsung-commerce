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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                    1L,
                    List.of(OrderV1Dto.OrderItem.builder().productId(productEntity.getId()).quantity(2L).build())
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, "gunny");

            ResponseEntity<ApiResponse<OrderV1Dto.Response.Place>> response = testRestTemplate.exchange(
                    BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers),
                    new ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Place>>() {});

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(10000L),
                    () -> assertThat(response.getBody().data().discountAmount()).isEqualTo(0L)
            );
        }

        @DisplayName("존재하지 않는 유저로 주문 시 404 Not Found 를 반환한다.")
        @Test
        void returnsNotFound_whenUserDoesNotExist() {
            // given: 상품만 DB에 있고, 유저는 없음
            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo")
                    .set(Select.field(Product::getPrice), 5000L)
                    .create();
            ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            OrderV1Dto.Request.Place requestBody = new OrderV1Dto.Request.Place(
                    1L,
                    List.of(OrderV1Dto.OrderItem.builder().productId(productEntity.getId()).quantity(2L).build())
            );
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, "nonexistentUser");

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.Place>> response = testRestTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    new ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Place>>() {});

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

        @DisplayName("존재하지 않는 상품 ID가 주문 항목에 포함되면 주문에 실패한다.")
        @Test
        void returnsFailure_whenProductIdDoesNotExist() {
            // given: 유저만 DB에 있고, 주문 항목의 productId는 DB에 없음
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            long nonExistentProductId = 99_999L;
            OrderV1Dto.Request.Place requestBody = new OrderV1Dto.Request.Place(
                    1L,
                    List.of(OrderV1Dto.OrderItem.builder().productId(nonExistentProductId).quantity(2L).build())
            );
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, "gunny");

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.Place>> response = testRestTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    new ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Place>>() {});

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().isError()),
                    () -> assertThat(response.getStatusCode().is2xxSuccessful()).isFalse()
            );
        }
    }
}
