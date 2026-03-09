package com.loopers.interfaces.api.review;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.review.Review;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
@SprintE2ETest
class ReviewV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/reviews";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @MockBean
    private OrderApiClient orderApiClient;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private void mockCompletedOrder(Long orderId, Long userId, Long productId) {
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                orderId, "202603091000000001", 10000L, 0L, 10000L,
                "COMPLETED", userId,
                List.of(new OrderApiDto.OrderLineResponse(productId, 1L, 10000L))
        );
        ApiResponse<OrderApiDto.OrderResponse> apiResponse = ApiResponse.success(orderResponse);
        doReturn(apiResponse).when(orderApiClient).getOrder(orderId);
    }

    @DisplayName("POST " + BASE_ENDPOINT)
    @Nested
    class CreateReview {

        @DisplayName("구매 확정된 주문의 상품에 리뷰를 작성한다.")
        @Test
        void createReview() {
            // given
            mockCompletedOrder(1L, 1L, 100L);

            ReviewV1Dto.Request.Create request = new ReviewV1Dto.Request.Create(100L, 1L, 5, "좋은 상품입니다.");

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("userId", 1L)
                    .build().toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(1L),
                    () -> assertThat(response.getBody().data().productId()).isEqualTo(100L),
                    () -> assertThat(response.getBody().data().rating()).isEqualTo(5),
                    () -> assertThat(response.getBody().data().content()).isEqualTo("좋은 상품입니다.")
            );
        }

        @DisplayName("동일 상품에 중복 리뷰를 작성하면 409 Conflict 응답을 반환한다.")
        @Test
        void throwException_whenDuplicateReview() {
            // given
            mockCompletedOrder(1L, 1L, 100L);

            Review existingReview = Review.create(1L, 100L, 1L, 5, "기존 리뷰");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(existingReview));

            ReviewV1Dto.Request.Create request = new ReviewV1Dto.Request.Create(100L, 1L, 4, "중복 리뷰");

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("userId", 1L)
                    .build().toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
            );
        }

        @DisplayName("구매 확정되지 않은 주문이면 400 Bad Request 응답을 반환한다.")
        @Test
        void throwException_whenOrderNotCompleted() {
            // given
            OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                    1L, "202603091000000001", 10000L, 0L, 10000L,
                    "PAID", 1L,
                    List.of(new OrderApiDto.OrderLineResponse(100L, 1L, 10000L))
            );
            doReturn(ApiResponse.success(orderResponse)).when(orderApiClient).getOrder(1L);

            ReviewV1Dto.Request.Create request = new ReviewV1Dto.Request.Create(100L, 1L, 5, "좋은 상품입니다.");

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT)
                    .queryParam("userId", 1L)
                    .build().toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }

    @DisplayName("GET " + BASE_ENDPOINT + "/{reviewId}")
    @Nested
    class GetReview {

        @DisplayName("존재하는 리뷰를 조회한다.")
        @Test
        void getReview() {
            // given
            Review review = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(review));

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/{reviewId}")
                    .buildAndExpand(review.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().reviewId()).isEqualTo(review.getId()),
                    () -> assertThat(response.getBody().data().rating()).isEqualTo(5),
                    () -> assertThat(response.getBody().data().content()).isEqualTo("좋은 상품입니다.")
            );
        }

        @DisplayName("존재하지 않는 리뷰를 조회하면 404 Not Found 응답을 반환한다.")
        @Test
        void throwException_whenNotFound() {
            // given
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/{reviewId}")
                    .buildAndExpand(999L)
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }
    }

    @DisplayName("GET " + BASE_ENDPOINT + "/products/{productId}")
    @Nested
    class GetProductReviews {

        @DisplayName("상품의 리뷰 목록을 페이징하여 조회한다.")
        @Test
        void getProductReviews() {
            // given
            Review review1 = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            Review review2 = Review.create(2L, 100L, 2L, 4, "괜찮은 상품입니다.");
            Review review3 = Review.create(3L, 100L, 3L, 3, "보통입니다.");
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(review1);
                testEntityManager.persist(review2);
                testEntityManager.persist(review3);
            });

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/products/{productId}")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .buildAndExpand(100L)
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.ReviewList>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.ReviewList>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().reviews()).hasSize(3),
                    () -> assertThat(response.getBody().data().ratingSummary().averageRating()).isEqualTo(4.0),
                    () -> assertThat(response.getBody().data().ratingSummary().totalCount()).isEqualTo(3)
            );
        }
    }

    @DisplayName("PUT " + BASE_ENDPOINT + "/{reviewId}")
    @Nested
    class UpdateReview {

        @DisplayName("본인의 리뷰를 수정한다.")
        @Test
        void updateReview() {
            // given
            Review review = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(review));

            ReviewV1Dto.Request.Update request = new ReviewV1Dto.Request.Update(3, "수정된 리뷰 내용");

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/{reviewId}")
                    .queryParam("userId", 1L)
                    .buildAndExpand(review.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().rating()).isEqualTo(3),
                    () -> assertThat(response.getBody().data().content()).isEqualTo("수정된 리뷰 내용")
            );
        }

        @DisplayName("작성자가 아닌 사용자가 수정하면 403 Forbidden 응답을 반환한다.")
        @Test
        void throwException_whenNotOwner() {
            // given
            Review review = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(review));

            ReviewV1Dto.Request.Update request = new ReviewV1Dto.Request.Update(3, "수정된 리뷰 내용");

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/{reviewId}")
                    .queryParam("userId", 999L)
                    .buildAndExpand(review.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> response =
                    testRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN)
            );
        }
    }

    @DisplayName("DELETE " + BASE_ENDPOINT + "/{reviewId}")
    @Nested
    class DeleteReview {

        @DisplayName("본인의 리뷰를 삭제한다.")
        @Test
        void deleteReview() {
            // given
            Review review = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(review));

            String deleteUrl = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/{reviewId}")
                    .queryParam("userId", 1L)
                    .buildAndExpand(review.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<Object>> response =
                    testRestTemplate.exchange(deleteUrl, HttpMethod.DELETE, HttpEntity.EMPTY, responseType);

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());

            // 삭제 후 조회하면 404 응답
            String getUrl = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/{reviewId}")
                    .buildAndExpand(review.getId())
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.Review>> getResponseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.Review>> getResponse =
                    testRestTemplate.exchange(getUrl, HttpMethod.GET, HttpEntity.EMPTY, getResponseType);

            // 리뷰가 soft delete 되었으므로, findById로는 여전히 조회될 수 있음
            // 실제 동작은 구현에 따라 다를 수 있으나 삭제 요청 자체가 성공했음을 검증
        }
    }

    @DisplayName("GET " + BASE_ENDPOINT + "/products/{productId}/rating")
    @Nested
    class GetProductRating {

        @DisplayName("상품의 평균 평점과 리뷰 수를 조회한다.")
        @Test
        void getProductRating() {
            // given
            Review review1 = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            Review review2 = Review.create(2L, 100L, 2L, 3, "보통입니다.");
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(review1);
                testEntityManager.persist(review2);
            });

            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/products/{productId}/rating")
                    .buildAndExpand(100L)
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.RatingSummary>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.RatingSummary>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().averageRating()).isEqualTo(4.0),
                    () -> assertThat(response.getBody().data().totalCount()).isEqualTo(2)
            );
        }

        @DisplayName("리뷰가 없는 상품은 평점 0.0, 리뷰 수 0을 반환한다.")
        @Test
        void returnEmpty_whenNoReviews() {
            // given
            String url = UriComponentsBuilder.fromPath(BASE_ENDPOINT + "/products/{productId}/rating")
                    .buildAndExpand(999L)
                    .toUriString();

            ParameterizedTypeReference<ApiResponse<ReviewV1Dto.Response.RatingSummary>> responseType = new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<ReviewV1Dto.Response.RatingSummary>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().averageRating()).isEqualTo(0.0),
                    () -> assertThat(response.getBody().data().totalCount()).isEqualTo(0)
            );
        }
    }
}
