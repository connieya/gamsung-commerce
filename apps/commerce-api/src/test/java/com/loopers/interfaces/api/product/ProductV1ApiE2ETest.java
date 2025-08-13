package com.loopers.interfaces.api.product;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.product.ProductSort;
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
import static org.assertj.core.api.Assertions.tuple;
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

        @DisplayName("상품 목록을 가격 낮은 순으로 정렬하여 조회한다.")
        @Test
        void getProducts_sortedByPrice_ascending() {
            // given
            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product1 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo1")
                    .set(Select.field(Product::getPrice), 10000L)
                    .create();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(ProductEntity.fromDomain(product1, brand)));

            Product product2 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo2")
                    .set(Select.field(Product::getPrice), 20000L)
                    .create();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(ProductEntity.fromDomain(product2, brand)));

            // when
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("productSort", ProductSort.PRICE_ASC)
                    .buildAndExpand()
                    .toUriString();

            ResponseEntity<ApiResponse<ProductV1Dto.SummaryResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
            });

            // then`
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().totalPage()).isEqualTo(1),
                    () -> assertThat(response.getBody().data().page()).isEqualTo(0),
                    () -> assertThat(response.getBody().data().size()).isEqualTo(10),
                    () -> assertThat(response.getBody().data().items()).hasSize(2)
                            .extracting("productName", "price", "likeCount")
                            .containsExactly(
                                    tuple("foo1", 10000L, 0L),
                                    tuple("foo2", 20000L, 0L)
                            )
            );

        }


        @DisplayName("상품 목록을 가격 높은 순으로 정렬하여 조회한다.")
        @Test
        void getProducts_sortedByPrice_descending() {
            // given
            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product1 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo1")
                    .set(Select.field(Product::getPrice), 10000L)
                    .create();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(ProductEntity.fromDomain(product1, brand)));

            Product product2 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo2")
                    .set(Select.field(Product::getPrice), 20000L)
                    .create();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(ProductEntity.fromDomain(product2, brand)));

            // when
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("productSort", ProductSort.PRICE_DESC)
                    .buildAndExpand()
                    .toUriString();

            ResponseEntity<ApiResponse<ProductV1Dto.SummaryResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
            });

            // then`
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().totalPage()).isEqualTo(1),
                    () -> assertThat(response.getBody().data().page()).isEqualTo(0),
                    () -> assertThat(response.getBody().data().size()).isEqualTo(10),
                    () -> assertThat(response.getBody().data().items()).hasSize(2)
                            .extracting("productName", "price", "likeCount")
                            .containsExactly(
                                    tuple("foo2", 20000L, 0L),
                                    tuple("foo1", 10000L, 0L)
                            )
            );

        }


        @DisplayName("상품 목록을 좋아요 개수 낮은 순으로 정렬하여 조회한다.")
        @Test
        void getProducts_sortedByLikes_ascending() {
            // given
            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product1 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo1")
                    .set(Select.field(Product::getPrice), 10000L)
                    .create();
            ProductEntity productEntity1 = ProductEntity.fromDomain(product1, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity1));

            Product product2 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo2")
                    .set(Select.field(Product::getPrice), 20000L)
                    .create();

            ProductEntity productEntity2 = ProductEntity.fromDomain(product2, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity2));

            Product product3 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo3")
                    .set(Select.field(Product::getPrice), 30000L)
                    .create();

            ProductEntity productEntity3 = ProductEntity.fromDomain(product3, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity3));


            transactionTemplate.executeWithoutResult(staus -> testEntityManager.persist(ProductLike.create(1L, productEntity1.getId())));
            transactionTemplate.executeWithoutResult(staus -> testEntityManager.persist(ProductLike.create(2L, productEntity1.getId())));
            transactionTemplate.executeWithoutResult(staus -> testEntityManager.persist(ProductLike.create(1L, productEntity2.getId())));

            // when
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("productSort", ProductSort.LIKES_ASC)
                    .buildAndExpand()
                    .toUriString();

            ResponseEntity<ApiResponse<ProductV1Dto.SummaryResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
            });

            // then`
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().totalPage()).isEqualTo(1),
                    () -> assertThat(response.getBody().data().page()).isEqualTo(0),
                    () -> assertThat(response.getBody().data().size()).isEqualTo(10),
                    () -> assertThat(response.getBody().data().items()).hasSize(3)
                            .extracting("productName", "price", "likeCount")
                            .containsExactly(
                                    tuple("foo3", 30000L, 0L),
                                    tuple("foo2", 20000L, 1L),
                                    tuple("foo1", 10000L, 2L)
                            )
            );

        }

        @DisplayName("상품 목록을 좋아요 개수 높은 순으로 정렬하여 조회한다.")
        @Test
        void getProducts_sortedByLikes_descending() {
            // given
            User user1 = UserFixture.complete().create();
            UserEntity userEntity1 = UserEntity.fromDomain(user1);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity1));

            User user2 = UserFixture.complete().create();
            UserEntity userEntity2 = UserEntity.fromDomain(user2);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity2));

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product1 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo1")
                    .set(Select.field(Product::getPrice), 10000L)
                    .create();
            ProductEntity productEntity1 = ProductEntity.fromDomain(product1, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity1));

            Product product2 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo2")
                    .set(Select.field(Product::getPrice), 20000L)
                    .create();

            ProductEntity productEntity2 = ProductEntity.fromDomain(product2, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity2));

            Product product3 = ProductFixture.complete()
                    .set(Select.field(Product::getName), "foo3")
                    .set(Select.field(Product::getPrice), 30000L)
                    .create();

            ProductEntity productEntity3 = ProductEntity.fromDomain(product3, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity3));


            transactionTemplate.executeWithoutResult(staus -> testEntityManager.persist(ProductLike.create(userEntity1.getId(), productEntity1.getId())));
            transactionTemplate.executeWithoutResult(staus -> testEntityManager.persist(ProductLike.create(userEntity2.getId(), productEntity1.getId())));
            transactionTemplate.executeWithoutResult(staus -> testEntityManager.persist(ProductLike.create(userEntity1.getId(), productEntity2.getId())));

            // when
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("productSort", ProductSort.LIKES_DESC)
                    .buildAndExpand()
                    .toUriString();

            ResponseEntity<ApiResponse<ProductV1Dto.SummaryResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
            });

            // then`
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().totalPage()).isEqualTo(1),
                    () -> assertThat(response.getBody().data().page()).isEqualTo(0),
                    () -> assertThat(response.getBody().data().size()).isEqualTo(10),
                    () -> assertThat(response.getBody().data().items()).hasSize(3)
                            .extracting("productName", "price", "likeCount")
                            .containsExactly(
                                    tuple("foo1", 10000L, 2L),
                                    tuple("foo2", 20000L, 1L),
                                    tuple("foo3", 30000L, 0L)
                            )
            );

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
                    ProductLike.create(userEntity1.getId(), productEntity.getId())
                    , ProductLike.create(userEntity2.getId(), productEntity.getId())
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
