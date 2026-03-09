package com.loopers.application.review;

import com.loopers.domain.review.ReviewCommand;
import com.loopers.domain.review.ReviewInfo;
import com.loopers.domain.review.ReviewRatingInfo;
import com.loopers.domain.review.ReviewService;
import com.loopers.domain.review.exception.ReviewException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewFacadeTest {

    @InjectMocks
    ReviewFacade reviewFacade;

    @Mock
    ReviewService reviewService;

    @Mock
    OrderVerifier orderVerifier;

    @DisplayName("리뷰 생성")
    @Nested
    class CreateReview {

        @DisplayName("구매 검증 후 리뷰를 생성한다.")
        @Test
        void createReview() {
            // given
            ReviewCriteria.Create criteria = new ReviewCriteria.Create(1L, 100L, 200L, 5, "좋은 상품입니다.");
            ZonedDateTime now = ZonedDateTime.now();
            ReviewInfo reviewInfo = new ReviewInfo(1L, 1L, 100L, 200L, 5, "좋은 상품입니다.", now, now);

            doNothing().when(orderVerifier).verifyPurchase(200L, 1L, 100L);
            doReturn(reviewInfo).when(reviewService).create(any(ReviewCommand.Create.class));

            // when
            ReviewInfo result = reviewFacade.createReview(criteria);

            // then
            assertAll(
                    () -> assertThat(result.userId()).isEqualTo(1L),
                    () -> assertThat(result.productId()).isEqualTo(100L),
                    () -> assertThat(result.orderId()).isEqualTo(200L),
                    () -> assertThat(result.rating()).isEqualTo(5)
            );
            verify(orderVerifier).verifyPurchase(200L, 1L, 100L);
            verify(reviewService).create(any(ReviewCommand.Create.class));
        }

        @DisplayName("구매 검증 실패 시 리뷰를 생성하지 않는다.")
        @Test
        void throwException_whenOrderNotCompleted() {
            // given
            ReviewCriteria.Create criteria = new ReviewCriteria.Create(1L, 100L, 200L, 5, "좋은 상품입니다.");

            doThrow(new ReviewException.ReviewOrderNotCompletedException(ErrorType.REVIEW_ORDER_NOT_COMPLETED))
                    .when(orderVerifier).verifyPurchase(200L, 1L, 100L);

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(criteria))
                    .isInstanceOf(ReviewException.ReviewOrderNotCompletedException.class);
        }
    }

    @DisplayName("리뷰 단건 조회")
    @Nested
    class GetReview {

        @DisplayName("리뷰 ID로 리뷰를 조회한다.")
        @Test
        void getReview() {
            // given
            ZonedDateTime now = ZonedDateTime.now();
            ReviewInfo reviewInfo = new ReviewInfo(1L, 1L, 100L, 200L, 5, "좋은 상품입니다.", now, now);
            doReturn(reviewInfo).when(reviewService).getReview(1L);

            // when
            ReviewInfo result = reviewFacade.getReview(1L);

            // then
            assertThat(result.reviewId()).isEqualTo(1L);
        }
    }

    @DisplayName("상품 리뷰 목록 조회")
    @Nested
    class GetProductReviews {

        @DisplayName("상품의 리뷰 목록과 평점 집계를 함께 조회한다.")
        @Test
        void getProductReviews() {
            // given
            ReviewCriteria.GetByProduct criteria = new ReviewCriteria.GetByProduct(100L, 0, 10);
            ZonedDateTime now = ZonedDateTime.now();
            ReviewInfo review1 = new ReviewInfo(1L, 1L, 100L, 200L, 5, "좋습니다.", now, now);
            ReviewInfo review2 = new ReviewInfo(2L, 2L, 100L, 201L, 4, "괜찮습니다.", now, now);
            Page<ReviewInfo> reviewPage = new PageImpl<>(List.of(review1, review2));

            ReviewRatingInfo ratingInfo = ReviewRatingInfo.of(4.5, 2);

            doReturn(reviewPage).when(reviewService).getReviewsByProductId(eq(100L), eq(0), eq(10));
            doReturn(ratingInfo).when(reviewService).getRatingInfo(100L);

            // when
            ReviewResult.ReviewList result = reviewFacade.getProductReviews(criteria);

            // then
            assertAll(
                    () -> assertThat(result.reviews()).hasSize(2),
                    () -> assertThat(result.ratingInfo().averageRating()).isEqualTo(4.5),
                    () -> assertThat(result.ratingInfo().totalCount()).isEqualTo(2)
            );
        }
    }

    @DisplayName("리뷰 수정")
    @Nested
    class UpdateReview {

        @DisplayName("Criteria를 Command로 변환하여 서비스에 전달한다.")
        @Test
        void updateReview() {
            // given
            ReviewCriteria.Update criteria = new ReviewCriteria.Update(1L, 1L, 3, "수정된 리뷰");
            ZonedDateTime now = ZonedDateTime.now();
            ReviewInfo updatedInfo = new ReviewInfo(1L, 1L, 100L, 200L, 3, "수정된 리뷰", now, now);

            doReturn(updatedInfo).when(reviewService).update(any(ReviewCommand.Update.class));

            // when
            ReviewInfo result = reviewFacade.updateReview(criteria);

            // then
            assertAll(
                    () -> assertThat(result.rating()).isEqualTo(3),
                    () -> assertThat(result.content()).isEqualTo("수정된 리뷰")
            );
            verify(reviewService).update(any(ReviewCommand.Update.class));
        }
    }

    @DisplayName("리뷰 삭제")
    @Nested
    class DeleteReview {

        @DisplayName("Criteria를 Command로 변환하여 서비스에 전달한다.")
        @Test
        void deleteReview() {
            // given
            ReviewCriteria.Delete criteria = new ReviewCriteria.Delete(1L, 1L);

            doNothing().when(reviewService).delete(any(ReviewCommand.Delete.class));

            // when
            reviewFacade.deleteReview(criteria);

            // then
            verify(reviewService).delete(any(ReviewCommand.Delete.class));
        }
    }

    @DisplayName("상품 평점 조회")
    @Nested
    class GetProductRating {

        @DisplayName("상품의 평균 평점을 조회한다.")
        @Test
        void getProductRating() {
            // given
            ReviewRatingInfo ratingInfo = ReviewRatingInfo.of(4.2, 15);
            doReturn(ratingInfo).when(reviewService).getRatingInfo(100L);

            // when
            ReviewRatingInfo result = reviewFacade.getProductRating(100L);

            // then
            assertAll(
                    () -> assertThat(result.averageRating()).isEqualTo(4.2),
                    () -> assertThat(result.totalCount()).isEqualTo(15)
            );
        }
    }
}
