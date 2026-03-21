package com.loopers.interfaces.api.sku;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.category.Category;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.sku.ProductSku;
import com.loopers.domain.product.sku.ProductSkuOption;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RequiredArgsConstructor
@SprintE2ETest
class SkuV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private Product persistProduct() {
        Brand brand = Brand.create("Nike", "Just Do It");
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

        Category category = Category.createRoot("상의", 1);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(category));

        Product product = Product.create("운동화", 100000L, brand, category.getId(), null, ZonedDateTime.now());
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

        return product;
    }

    @DisplayName("GET /api/v1/products/{productId}/options")
    @Nested
    class GetOptions {

        @DisplayName("상품별 옵션 목록을 조회한다.")
        @Test
        void getOptions_success() {
            // given
            Product product = persistProduct();

            ProductOption option1 = ProductOption.create(product.getId(), OptionType.COLOR, "빨강");
            ProductOption option2 = ProductOption.create(product.getId(), OptionType.SIZE, "M");
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(option1);
                testEntityManager.persist(option2);
            });

            String url = UriComponentsBuilder.fromPath("/api/v1/products/{productId}/options")
                    .buildAndExpand(product.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.OptionList>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.OptionList>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data().options()).hasSize(2)
            );
        }
    }

    @DisplayName("GET /api/v1/skus/{skuId}")
    @Nested
    class GetSku {

        @DisplayName("SKU를 정상적으로 조회한다.")
        @Test
        void getSku_success() {
            // given
            Product product = persistProduct();

            ProductOption colorOption = ProductOption.create(product.getId(), OptionType.COLOR, "빨강");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(colorOption));

            ProductSku sku = ProductSku.create(product.getId(), "P001-RED", 0L);
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(sku);
                testEntityManager.persist(ProductSkuOption.create(sku, colorOption.getId()));
            });

            String url = UriComponentsBuilder.fromPath("/api/v1/skus/{skuId}")
                    .buildAndExpand(sku.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Sku>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Sku>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data().skuCode()).isEqualTo("P001-RED"),
                    () -> assertThat(response.getBody().data().options()).hasSize(1)
            );
        }

        @DisplayName("존재하지 않는 SKU ID로 조회 시 404를 반환한다.")
        @Test
        void throwException_whenSkuNotFound() {
            // given
            String url = UriComponentsBuilder.fromPath("/api/v1/skus/{skuId}")
                    .buildAndExpand(9999L)
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Sku>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Sku>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("GET /api/v1/products/{productId}/skus")
    @Nested
    class GetSkusByProduct {

        @DisplayName("상품별 SKU 목록을 조회한다.")
        @Test
        void getSkusByProduct_success() {
            // given
            Product product = persistProduct();

            ProductOption colorOption = ProductOption.create(product.getId(), OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(product.getId(), OptionType.SIZE, "M");
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(colorOption);
                testEntityManager.persist(sizeOption);
            });

            ProductSku sku1 = ProductSku.create(product.getId(), "P001-RED", 0L);
            ProductSku sku2 = ProductSku.create(product.getId(), "P001-M", 0L);
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(sku1);
                testEntityManager.persist(ProductSkuOption.create(sku1, colorOption.getId()));
                testEntityManager.persist(sku2);
                testEntityManager.persist(ProductSkuOption.create(sku2, sizeOption.getId()));
            });

            String url = UriComponentsBuilder.fromPath("/api/v1/products/{productId}/skus")
                    .buildAndExpand(product.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.SkuList>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.SkuList>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data().skus()).hasSize(2)
            );
        }
    }
}
