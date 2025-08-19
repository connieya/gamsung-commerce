package com.loopers.interfaces.api.brand;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.brand.Brand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
@SprintE2ETest
public class BrandV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/brands";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET" + BASE_ENDPOINT + "/{brandId}")
    @Nested
    class GetBrand {
        private static final String REQUEST_URL = BASE_ENDPOINT + "/{brandId}";

        @Test
        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        void throwException_whenBrandDoesNotExist() {
            // given
            Long brandId = 1L;

            HttpEntity<?> requestEntity = HttpEntity.EMPTY;
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(brandId)
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>>() {
            };

            // when
            ResponseEntity<ApiResponse<BrandV1Dto.BrandResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }


        @Test
        @DisplayName("브랜드 조회에 성공할 경우, 브랜드 정보를 반환한다. ")
        void getBrandIno() {
            // given
            Brand brand = Brand.builder()
                    .name("Nike")
                    .description("Just Do It.")
                    .build();

            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));


            HttpEntity<?> requestEntity = HttpEntity.EMPTY;
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(brand.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>>() {
            };

            // when
            ResponseEntity<ApiResponse<BrandV1Dto.BrandResponse>> response = testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().id()).isEqualTo(brand.getId()),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(brand.getName()),
                    () -> assertThat(response.getBody().data().description()).isEqualTo(brand.getDescription())
            );
        }
    }

}
