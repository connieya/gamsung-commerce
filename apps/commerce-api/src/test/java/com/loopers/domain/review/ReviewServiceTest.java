package com.loopers.domain.review;

import com.loopers.domain.review.exception.ReviewException;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    ReviewService reviewService;

    @Mock
    ReviewRepository reviewRepository;

    @DisplayName("리뷰 생성")
    @Nested
    class Create {

        @DisplayName("정상적으로 리뷰를 생성한다.")
        @Test
        void createReview() {
            // given
            ReviewCommand.Create command = new ReviewCommand.Create(1L, 1L, 1L, 5, "좋은 상품입니다.");
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            doReturn(false).when(reviewRepository).existsByUserIdAndProductId(1L, 1L);
            doReturn(review).when(reviewRepository).save(any(Review.class));

            // when
            ReviewInfo result = reviewService.create(command);

            // then
            assertAll(
                    () -> assertThat(result.userId()).isEqualTo(1L),
                    () -> assertThat(result.productId()).isEqualTo(1L),
                    () -> assertThat(result.rating()).isEqualTo(5),
                    () -> assertThat(result.content()).isEqualTo("좋은 상품입니다.")
            );
            verify(reviewRepository).save(any(Review.class));
        }

        @DisplayName("이미 리뷰를 작성한 상품이면 예외가 발생한다.")
        @Test
        void throwException_whenAlreadyExists() {
            // given
            ReviewCommand.Create command = new ReviewCommand.Create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            doReturn(true).when(reviewRepository).existsByUserIdAndProductId(1L, 1L);

            // when & then
            assertThatThrownBy(() -> reviewService.create(command))
                    .isInstanceOf(ReviewException.ReviewAlreadyExistsException.class);
        }
    }

    @DisplayName("리뷰 단건 조회")
    @Nested
    class GetReview {

        @DisplayName("존재하는 리뷰를 조회한다.")
        @Test
        void getReview() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");
            doReturn(Optional.of(review)).when(reviewRepository).findById(1L);

            // when
            ReviewInfo result = reviewService.getReview(1L);

            // then
            assertAll(
                    () -> assertThat(result.userId()).isEqualTo(1L),
                    () -> assertThat(result.rating()).isEqualTo(5)
            );
        }

        @DisplayName("존재하지 않는 리뷰를 조회하면 예외가 발생한다.")
        @Test
        void throwException_whenNotFound() {
            // given
            doReturn(Optional.empty()).when(reviewRepository).findById(999L);

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(999L))
                    .isInstanceOf(ReviewException.ReviewNotFoundException.class);
        }
    }

    @DisplayName("상품별 리뷰 목록 조회")
    @Nested
    class GetReviewsByProductId {

        @DisplayName("상품의 리뷰 목록을 페이징하여 조회한다.")
        @Test
        void getReviewsByProductId() {
            // given
            Review review1 = Review.create(1L, 100L, 1L, 5, "좋은 상품입니다.");
            Review review2 = Review.create(2L, 100L, 2L, 4, "괜찮은 상품입니다.");
            Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), PageRequest.of(0, 10), 2);

            doReturn(reviewPage).when(reviewRepository).findByProductId(eq(100L), any());

            // when
            Page<ReviewInfo> result = reviewService.getReviewsByProductId(100L, 0, 10);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(2),
                    () -> assertThat(result.getContent().get(0).rating()).isEqualTo(5),
                    () -> assertThat(result.getContent().get(1).rating()).isEqualTo(4)
            );
        }
    }

    @DisplayName("리뷰 수정")
    @Nested
    class Update {

        @DisplayName("본인의 리뷰를 수정한다.")
        @Test
        void updateReview() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");
            doReturn(Optional.of(review)).when(reviewRepository).findById(1L);

            ReviewCommand.Update command = new ReviewCommand.Update(1L, 1L, 3, "수정된 리뷰");

            // when
            ReviewInfo result = reviewService.update(command);

            // then
            assertAll(
                    () -> assertThat(result.rating()).isEqualTo(3),
                    () -> assertThat(result.content()).isEqualTo("수정된 리뷰")
            );
        }

        @DisplayName("존재하지 않는 리뷰를 수정하면 예외가 발생한다.")
        @Test
        void throwException_whenNotFound() {
            // given
            doReturn(Optional.empty()).when(reviewRepository).findById(999L);
            ReviewCommand.Update command = new ReviewCommand.Update(999L, 1L, 3, "수정된 리뷰");

            // when & then
            assertThatThrownBy(() -> reviewService.update(command))
                    .isInstanceOf(ReviewException.ReviewNotFoundException.class);
        }

        @DisplayName("작성자가 아닌 사용자가 수정하면 예외가 발생한다.")
        @Test
        void throwException_whenNotOwner() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");
            doReturn(Optional.of(review)).when(reviewRepository).findById(1L);

            ReviewCommand.Update command = new ReviewCommand.Update(1L, 999L, 3, "수정된 리뷰");

            // when & then
            assertThatThrownBy(() -> reviewService.update(command))
                    .isInstanceOf(CoreException.class);
        }
    }

    @DisplayName("리뷰 삭제")
    @Nested
    class Delete {

        @DisplayName("본인의 리뷰를 삭제한다.")
        @Test
        void deleteReview() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");
            doReturn(Optional.of(review)).when(reviewRepository).findById(1L);

            ReviewCommand.Delete command = new ReviewCommand.Delete(1L, 1L);

            // when
            reviewService.delete(command);

            // then
            assertThat(review.getDeletedAt()).isNotNull();
        }

        @DisplayName("존재하지 않는 리뷰를 삭제하면 예외가 발생한다.")
        @Test
        void throwException_whenNotFound() {
            // given
            doReturn(Optional.empty()).when(reviewRepository).findById(999L);
            ReviewCommand.Delete command = new ReviewCommand.Delete(999L, 1L);

            // when & then
            assertThatThrownBy(() -> reviewService.delete(command))
                    .isInstanceOf(ReviewException.ReviewNotFoundException.class);
        }

        @DisplayName("작성자가 아닌 사용자가 삭제하면 예외가 발생한다.")
        @Test
        void throwException_whenNotOwner() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");
            doReturn(Optional.of(review)).when(reviewRepository).findById(1L);

            ReviewCommand.Delete command = new ReviewCommand.Delete(1L, 999L);

            // when & then
            assertThatThrownBy(() -> reviewService.delete(command))
                    .isInstanceOf(CoreException.class);
        }
    }

    @DisplayName("평점 집계 조회")
    @Nested
    class GetRatingInfo {

        @DisplayName("상품의 평균 평점과 리뷰 수를 조회한다.")
        @Test
        void getRatingInfo() {
            // given
            ReviewRatingInfo ratingInfo = ReviewRatingInfo.of(4.5, 10);
            doReturn(ratingInfo).when(reviewRepository).getAverageRatingByProductId(1L);

            // when
            ReviewRatingInfo result = reviewService.getRatingInfo(1L);

            // then
            assertAll(
                    () -> assertThat(result.averageRating()).isEqualTo(4.5),
                    () -> assertThat(result.totalCount()).isEqualTo(10)
            );
        }

        @DisplayName("리뷰가 없는 상품은 빈 평점 정보를 반환한다.")
        @Test
        void returnEmpty_whenNoReviews() {
            // given
            doReturn(ReviewRatingInfo.empty()).when(reviewRepository).getAverageRatingByProductId(1L);

            // when
            ReviewRatingInfo result = reviewService.getRatingInfo(1L);

            // then
            assertAll(
                    () -> assertThat(result.averageRating()).isEqualTo(0.0),
                    () -> assertThat(result.totalCount()).isEqualTo(0)
            );
        }
    }
}
