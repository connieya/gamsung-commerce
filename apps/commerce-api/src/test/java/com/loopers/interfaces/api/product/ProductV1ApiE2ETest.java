package com.loopers.interfaces.api.product;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.common.Sort;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.likes.ProductLike;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
@SprintE2ETest
class ProductV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/products";

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET" + BASE_ENDPOINT)
    @Nested
    class GetProductDetails {

        @DisplayName("상품 목록 조회")
        @Test
        void getProducts() {
            // given

            // when
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("sort", Sort.LATEST)
                    .buildAndExpand()
                    .toUriString();

            ResponseEntity<ApiResponse<ProductV1Dto.SummaryResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
            });

            // then`
        }

    }


    @DisplayName("GET" + BASE_ENDPOINT + "/{productId}")
    @Nested
    class GetProductDetail {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/{productId}";


        @DisplayName("상품 id 에 해당하는 상품 정보를 조회한다. ")
        @Test
        void getProductDetail() {
            // given
            Brand brand = Brand.create("nike", "just do it!");
            Product product = ProductFixture.complete().set(Select.field(Product::getPrice), 10000L).create();
            ProductEntity productEntity = ProductEntity.fromDomain(product, brand);

            User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
            UserEntity userEntity1 = UserEntity.fromDomain(user1);
            UserEntity userEntity2 = UserEntity.fromDomain(user2);


            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));
            transactionTemplate.executeWithoutResult(status -> List.of(
                    userEntity1, userEntity2).forEach(testEntityManager::persist)
            );

            List<ProductLike> productLikeEntities = List.of(
                    ProductLike.create(userEntity1, productEntity)
                    , ProductLike.create(userEntity2, productEntity)
            );

            transactionTemplate.executeWithoutResult(status -> productLikeEntities.forEach(testEntityManager::persist));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(productEntity.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ProductV1Dto.DetailResponse>> responseType = new ParameterizedTypeReference<ApiResponse<ProductV1Dto.DetailResponse>>() {
            };

            ResponseEntity<ApiResponse<ProductV1Dto.DetailResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().brandName()).isEqualTo("nike"),
                    () -> assertThat(response.getBody().data().price()).isEqualTo(10000L),
                    () -> assertThat(response.getBody().data().productId()).isEqualTo(productEntity.getId()),
                    () -> assertThat(response.getBody().data().likeCount()).isEqualTo(2)

            );
        }

    }


}
