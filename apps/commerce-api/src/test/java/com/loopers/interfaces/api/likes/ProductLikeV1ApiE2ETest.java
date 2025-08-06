package com.loopers.interfaces.api.likes;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.brand.BrandEntity;
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
import org.springframework.http.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@SprintE2ETest
@RequiredArgsConstructor
class ProductLikeV1ApiE2ETest {

    private static final String ENDPOINT = "/api/v1/like/products";

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST" + ENDPOINT + "/{productId}")
    @Nested
    class LikeAdd{
        private static final String REQUEST_URL = ENDPOINT +"/{productId}";

        @DisplayName("올바른 상품 ID, 유저 ID가 주어지면 상품을 좋아요 할 수 있다.")
        @Test
        void addLikeSuccess() {
            // give
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            BrandEntity brandEntity = BrandEntity.fromDomain(Brand.create("nike", "just do it!"));
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brandEntity));

            Product product = ProductFixture.complete().create();
            ProductEntity productEntity = ProductEntity.fromDomain(product ,brandEntity);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(productEntity.getId())
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID ,"gunny");
            // then
            ResponseEntity<Void> response = testRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        }

        @DisplayName("존재하지 않는 상품 ID가 주어지면, 상품 좋아요에 실패한다.")
        @Test
        void likeProduct_whenInvalidProductId() {
            // give
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            BrandEntity brandEntity = BrandEntity.fromDomain(Brand.create("nike", "just do it!"));
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brandEntity));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(1L)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID ,"gunny");

            // then
            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });

            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().meta().message()).isEqualTo("존재하지 않는 상품입니다.");

        }
    }

    @DisplayName("DELETE" + ENDPOINT +"/{productId}")
    @Nested
    class likeRemove {

        private static final String REQUEST_URL = ENDPOINT +"/{productId}";

        @DisplayName("올바른 상품 ID, 유저 ID가 주어지면 상품 좋아요를 취소할 수 있다.")
        @Test
        void removeLikeSuccess() {
            // give
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            BrandEntity brandEntity = BrandEntity.fromDomain(Brand.create("nike", "just do it!"));
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brandEntity));

            Product product = ProductFixture.complete().create();
            ProductEntity productEntity = ProductEntity.fromDomain(product ,brandEntity);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(1L)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID ,"gunny");
            // then
            ResponseEntity<Void> response = testRestTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }
    }

}
