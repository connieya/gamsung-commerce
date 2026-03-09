package com.loopers.interfaces.api.like;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.infrastructure.feign.commerce.CommerceApiClient;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
import com.loopers.interfaces.api.ApiHeaders;
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
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@SprintE2ETest
class LikeV1ApiE2ETest {

    private static final String BASE_URL = "/api/v1/likes";
    private static final String USER_ID = "testuser";
    private static final Long USER_DB_ID = 1L;

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @MockitoBean
    private CommerceApiClient commerceApiClient;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.USER_ID, USER_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void mockUser() {
        CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(USER_DB_ID, USER_ID, "test@test.com");
        when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));
    }

    private void mockProducts() {
        List<CommerceApiDto.ProductResponse> products = List.of(
                new CommerceApiDto.ProductResponse(100L, "상품A", 5000L, "http://img.com/a.jpg", "브랜드A"),
                new CommerceApiDto.ProductResponse(200L, "상품B", 10000L, "http://img.com/b.jpg", "브랜드B")
        );
        when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(products));
    }

    private void persistLike(Long userId, Long targetId, LikeTargetType targetType) {
        transactionTemplate.executeWithoutResult(status -> {
            Like like = Like.create(userId, targetId, targetType);
            testEntityManager.persist(like);
            testEntityManager.persist(LikeSummary.create(targetId, targetType));
        });
    }

    @Nested
    @DisplayName("POST /api/v1/likes/{targetType}/{targetId}")
    class AddLike {

        @Test
        @DisplayName("좋아요를 추가하면 count가 반환된다")
        void add_success() {
            // given
            mockUser();

            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response.LikeAction>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<LikeV1Dto.Response.LikeAction>> response =
                    testRestTemplate.exchange(BASE_URL + "/PRODUCT/100", HttpMethod.POST,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().likeTargetType()).isEqualTo(LikeTargetType.PRODUCT),
                    () -> assertThat(response.getBody().data().targetId()).isEqualTo(100L),
                    () -> assertThat(response.getBody().data().count()).isEqualTo(1L)
            );
        }

        @Test
        @DisplayName("이미 좋아요한 경우 멱등하게 동작한다")
        void add_idempotent() {
            // given
            mockUser();
            persistLike(USER_DB_ID, 100L, LikeTargetType.PRODUCT);

            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response.LikeAction>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<LikeV1Dto.Response.LikeAction>> response =
                    testRestTemplate.exchange(BASE_URL + "/PRODUCT/100", HttpMethod.POST,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().count()).isEqualTo(0L)
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/likes/{targetType}/{targetId}")
    class RemoveLike {

        @Test
        @DisplayName("좋아요를 취소하면 감소된 count가 반환된다")
        void remove_success() {
            // given
            mockUser();

            // 먼저 좋아요 추가
            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response.LikeAction>> responseType =
                    new ParameterizedTypeReference<>() {};
            testRestTemplate.exchange(BASE_URL + "/PRODUCT/100", HttpMethod.POST,
                    new HttpEntity<>(null, createHeaders()), responseType);

            // when
            ResponseEntity<ApiResponse<LikeV1Dto.Response.LikeAction>> response =
                    testRestTemplate.exchange(BASE_URL + "/PRODUCT/100", HttpMethod.DELETE,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().count()).isEqualTo(0L)
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/likes/{targetType}")
    class GetMyLikes {

        @Test
        @DisplayName("내 좋아요 목록을 반환한다")
        void getMyLikes_success() {
            // given
            mockUser();
            mockProducts();

            // 좋아요 2개 추가
            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response.LikeAction>> actionType =
                    new ParameterizedTypeReference<>() {};
            testRestTemplate.exchange(BASE_URL + "/PRODUCT/100", HttpMethod.POST,
                    new HttpEntity<>(null, createHeaders()), actionType);
            testRestTemplate.exchange(BASE_URL + "/PRODUCT/200", HttpMethod.POST,
                    new HttpEntity<>(null, createHeaders()), actionType);

            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response.LikedProducts>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<LikeV1Dto.Response.LikedProducts>> response =
                    testRestTemplate.exchange(BASE_URL + "/PRODUCT", HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).hasSize(2)
            );
        }

        @Test
        @DisplayName("좋아요가 없으면 빈 목록을 반환한다")
        void getMyLikes_empty() {
            // given
            mockUser();

            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response.LikedProducts>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<LikeV1Dto.Response.LikedProducts>> response =
                    testRestTemplate.exchange(BASE_URL + "/PRODUCT", HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).isEmpty()
            );
        }
    }
}
