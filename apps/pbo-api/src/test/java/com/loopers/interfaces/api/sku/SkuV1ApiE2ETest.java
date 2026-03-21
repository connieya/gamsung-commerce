package com.loopers.interfaces.api.sku;

import com.loopers.annotation.SprintE2ETest;
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

import java.util.List;

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
        Product product = Product.create();
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));
        return product;
    }

    @DisplayName("POST /api/v1/products/{productId}/options")
    @Nested
    class RegisterOption {

        @DisplayName("상품 옵션을 정상적으로 등록하고 201을 반환한다.")
        @Test
        void registerOption_success() {
            // given
            Product product = persistProduct();

            String url = UriComponentsBuilder.fromPath("/api/v1/products/{productId}/options")
                    .buildAndExpand(product.getId())
                    .toUriString();

            SkuV1Dto.Request.RegisterOption request = new SkuV1Dto.Request.RegisterOption(OptionType.COLOR, "빨강");
            HttpEntity<SkuV1Dto.Request.RegisterOption> requestEntity = new HttpEntity<>(request);

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Option>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Option>> response =
                    testRestTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody().data().productId()).isEqualTo(product.getId()),
                    () -> assertThat(response.getBody().data().optionType()).isEqualTo(OptionType.COLOR),
                    () -> assertThat(response.getBody().data().optionValue()).isEqualTo("빨강")
            );
        }

        @DisplayName("존재하지 않는 상품에 옵션 등록 시 404를 반환한다.")
        @Test
        void throwException_whenProductNotFound() {
            // given
            String url = UriComponentsBuilder.fromPath("/api/v1/products/{productId}/options")
                    .buildAndExpand(9999L)
                    .toUriString();

            SkuV1Dto.Request.RegisterOption request = new SkuV1Dto.Request.RegisterOption(OptionType.COLOR, "빨강");
            HttpEntity<SkuV1Dto.Request.RegisterOption> requestEntity = new HttpEntity<>(request);

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Option>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Option>> response =
                    testRestTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("POST /api/v1/skus")
    @Nested
    class RegisterSku {

        @DisplayName("SKU를 정상적으로 등록하고 201을 반환한다.")
        @Test
        void registerSku_success() {
            // given
            Product product = persistProduct();

            ProductOption colorOption = ProductOption.create(product.getId(), OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(product.getId(), OptionType.SIZE, "M");
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(colorOption);
                testEntityManager.persist(sizeOption);
            });

            SkuV1Dto.Request.RegisterSku request = new SkuV1Dto.Request.RegisterSku(
                    product.getId(), "P001-RED-M", 0L, List.of(colorOption.getId(), sizeOption.getId())
            );
            HttpEntity<SkuV1Dto.Request.RegisterSku> requestEntity = new HttpEntity<>(request);

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Sku>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Sku>> response =
                    testRestTemplate.exchange("/api/v1/skus", HttpMethod.POST, requestEntity, responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody().data().productId()).isEqualTo(product.getId()),
                    () -> assertThat(response.getBody().data().skuCode()).isEqualTo("P001-RED-M"),
                    () -> assertThat(response.getBody().data().options()).hasSize(2)
            );
        }

        @DisplayName("동일한 옵션 조합으로 SKU 등록 시 409를 반환한다.")
        @Test
        void throwException_whenDuplicateOptionCombination() {
            // given
            Product product = persistProduct();

            ProductOption colorOption = ProductOption.create(product.getId(), OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(product.getId(), OptionType.SIZE, "M");
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(colorOption);
                testEntityManager.persist(sizeOption);
            });

            // 첫 번째 SKU 등록
            ProductSku existingSku = ProductSku.create(product.getId(), "P001-RED-M-1", 0L);
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(existingSku);
                testEntityManager.persist(ProductSkuOption.create(existingSku, colorOption.getId()));
                testEntityManager.persist(ProductSkuOption.create(existingSku, sizeOption.getId()));
            });

            // 두 번째 SKU 등록 (동일 옵션 조합)
            SkuV1Dto.Request.RegisterSku request = new SkuV1Dto.Request.RegisterSku(
                    product.getId(), "P001-RED-M-2", 0L, List.of(colorOption.getId(), sizeOption.getId())
            );
            HttpEntity<SkuV1Dto.Request.RegisterSku> requestEntity = new HttpEntity<>(request);

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Sku>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Sku>> response =
                    testRestTemplate.exchange("/api/v1/skus", HttpMethod.POST, requestEntity, responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @DisplayName("타 상품 옵션 사용 시 400을 반환한다.")
        @Test
        void throwException_whenOptionBelongsToAnotherProduct() {
            // given
            Product product = persistProduct();
            Product anotherProduct = persistProduct();

            ProductOption anotherOption = ProductOption.create(anotherProduct.getId(), OptionType.COLOR, "파랑");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(anotherOption));

            SkuV1Dto.Request.RegisterSku request = new SkuV1Dto.Request.RegisterSku(
                    product.getId(), "P001-BLUE", 0L, List.of(anotherOption.getId())
            );
            HttpEntity<SkuV1Dto.Request.RegisterSku> requestEntity = new HttpEntity<>(request);

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Sku>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Sku>> response =
                    testRestTemplate.exchange("/api/v1/skus", HttpMethod.POST, requestEntity, responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("존재하지 않는 상품 ID로 SKU 등록 시 404를 반환한다.")
        @Test
        void throwException_whenProductNotFound() {
            // given
            SkuV1Dto.Request.RegisterSku request = new SkuV1Dto.Request.RegisterSku(
                    9999L, "P001-RED-M", 0L, List.of(1L)
            );
            HttpEntity<SkuV1Dto.Request.RegisterSku> requestEntity = new HttpEntity<>(request);

            ParameterizedTypeReference<ApiResponse<SkuV1Dto.Response.Sku>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<SkuV1Dto.Response.Sku>> response =
                    testRestTemplate.exchange("/api/v1/skus", HttpMethod.POST, requestEntity, responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
