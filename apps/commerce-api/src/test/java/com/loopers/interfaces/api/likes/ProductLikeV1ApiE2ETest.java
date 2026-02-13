package com.loopers.interfaces.api.likes;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.likes.ProductLike;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
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

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;

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
    class LikeAdd {
        private static final String REQUEST_URL = ENDPOINT + "/{productId}";

        @DisplayName("올바른 상품 ID, 유저 ID가 주어지면 상품을 좋아요 할 수 있다.")
        @Test
        void addLikeSuccess() {
            // give
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product = ProductFixture.complete().create();
            ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(productEntity.getId())
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID, "gunny");
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

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(1L)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID, "gunny");

            // then
            ResponseEntity<ApiResponse<Void>> response = testRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });

            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().meta().message()).isEqualTo("존재하지 않는 상품입니다.");

        }
    }

    @DisplayName("DELETE" + ENDPOINT + "/{productId}")
    @Nested
    class likeRemove {

        private static final String REQUEST_URL = ENDPOINT + "/{productId}";

        @DisplayName("올바른 상품 ID, 유저 ID가 주어지면 상품 좋아요를 취소할 수 있다.")
        @Test
        void removeLikeSuccess() {
            // give
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product = ProductFixture.complete().create();
            ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            // when
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(1L)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID, "gunny");
            // then
            ResponseEntity<Void> response = testRestTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }
    }

    @DisplayName("GET" + ENDPOINT)
    @Nested
    class GetLikeProduct {

        @DisplayName("좋아요 한 상품 목록을 조회할 수 있다.")
        @Test
        void getLikeProduct() {
            // given
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            Integer initialLikeCount = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(0, 20))
                    .create();

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            List<ProductEntity> productEntities = IntStream.range(0, initialLikeCount)
                    .mapToObj(i ->
                            ProductEntity.fromDomain(
                                    ProductFixture.complete()
                                            .set(Select.field(Product::getName), "product" + i)
                                            .create(),
                                    brand
                            )
                    )
                    .toList();

            transactionTemplate.executeWithoutResult(status ->
                    productEntities.forEach(testEntityManager::persist)
            );


            transactionTemplate.executeWithoutResult(status ->
                    productEntities.forEach(productEntity -> {
                        ProductLike productLike = ProductLike.create(userEntity.getId(), productEntity.getId());
                        testEntityManager.persist(productLike);
                    })
            );

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID, "gunny");

            ResponseEntity<ApiResponse<ProductLikeV1Dto.LikedProductResponse>> response = testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().likedProducts()).hasSize(initialLikeCount);

        }

        @DisplayName("좋아요 목록 조회 시 각 상품 항목에 productId, productName, productPrice, brandName, likeCount가 포함된다 (API 응답 body 검증)")
        @Test
        void getMyLikes_responseBody_containsRequiredFields() {
            // given: 유저, 브랜드, 상품, 좋아요 데이터
            User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
            UserEntity userEntity = UserEntity.fromDomain(user);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(userEntity));

            Brand brand = Brand.create("nike", "just do it!");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            Product product = ProductFixture.complete().set(Select.field(Product::getName), "나이키 에어맥스").create();
            ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productEntity));

            ProductLike productLike = ProductLike.create(userEntity.getId(), productEntity.getId());
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(productLike));

            // when: GET /api/v1/like/products
            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeaders.USER_ID, "gunny");
            ResponseEntity<ApiResponse<LikedProductResponseBody>> response = testRestTemplate.exchange(
                    ENDPOINT,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            // then: status 200, data.likedProducts 크기 1
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data()).isNotNull();
            assertThat(response.getBody().data().likedProducts()).hasSize(1);

            // then: 각 항목에 필수 필드가 존재하고 null이 아니다 (빈 객체 {} 응답 시 실패)
            LikedProductItemBody item = response.getBody().data().likedProducts().get(0);
            assertThat(item.productId())
                    .as("likedProducts[].productId가 응답에 포함되어야 한다")
                    .isNotNull();
            assertThat(item.productName())
                    .as("likedProducts[].productName이 응답에 포함되어야 한다")
                    .isNotNull();
            assertThat(item.productPrice())
                    .as("likedProducts[].productPrice가 응답에 포함되어야 한다")
                    .isNotNull();
            assertThat(item.brandName())
                    .as("likedProducts[].brandName이 응답에 포함되어야 한다")
                    .isNotNull();
            assertThat(item.likeCount())
                    .as("likedProducts[].likeCount가 응답에 포함되어야 한다")
                    .isNotNull();

            // then: 실제 저장한 값과 일치
            assertThat(item.productId()).isEqualTo(productEntity.getId());
            assertThat(item.productName()).isEqualTo("나이키 에어맥스");
            assertThat(item.brandName()).isEqualTo("nike");
        }
    }

    /** API 응답 body 검증용 DTO (테스트 전용, getter 필요) */
    private record LikedProductResponseBody(java.util.List<LikedProductItemBody> likedProducts) {}

    private record LikedProductItemBody(Long productId, String productName, Long productPrice, String brandName, Long likeCount) {}

}
