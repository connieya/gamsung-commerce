package com.loopers.domain.review;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReviewTest {

    @DisplayName("리뷰 생성")
    @Nested
    class Create {

        @DisplayName("정상적인 값으로 리뷰를 생성한다.")
        @Test
        void createReview() {
            // when
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            // then
            assertAll(
                    () -> assertThat(review.getUserId()).isEqualTo(1L),
                    () -> assertThat(review.getProductId()).isEqualTo(1L),
                    () -> assertThat(review.getOrderId()).isEqualTo(1L),
                    () -> assertThat(review.getRating()).isEqualTo(5),
                    () -> assertThat(review.getContent()).isEqualTo("좋은 상품입니다.")
            );
        }

        @DisplayName("사용자 ID가 null이면 CoreException이 발생한다.")
        @Test
        void throwException_withNullUserId() {
            assertThatThrownBy(() -> Review.create(null, 1L, 1L, 5, "좋은 상품입니다."))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 ID가 null이면 CoreException이 발생한다.")
        @Test
        void throwException_withNullProductId() {
            assertThatThrownBy(() -> Review.create(1L, null, 1L, 5, "좋은 상품입니다."))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("주문 ID가 null이면 CoreException이 발생한다.")
        @Test
        void throwException_withNullOrderId() {
            assertThatThrownBy(() -> Review.create(1L, 1L, null, 5, "좋은 상품입니다."))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("평점이 유효 범위(1~5)를 벗어나면 CoreException이 발생한다.")
        @ValueSource(ints = {0, -1, 6, 100})
        @ParameterizedTest
        void throwException_withInvalidRating(int rating) {
            assertThatThrownBy(() -> Review.create(1L, 1L, 1L, rating, "좋은 상품입니다."))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.REVIEW_INVALID_RATING);
        }

        @DisplayName("리뷰 내용이 비어있으면 CoreException이 발생한다.")
        @ValueSource(strings = {"", " "})
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidContent(String content) {
            assertThatThrownBy(() -> Review.create(1L, 1L, 1L, 5, content))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("리뷰 수정")
    @Nested
    class Update {

        @DisplayName("정상적인 값으로 리뷰를 수정한다.")
        @Test
        void updateReview() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            // when
            review.update(3, "수정된 리뷰 내용");

            // then
            assertAll(
                    () -> assertThat(review.getRating()).isEqualTo(3),
                    () -> assertThat(review.getContent()).isEqualTo("수정된 리뷰 내용")
            );
        }

        @DisplayName("수정 시 평점이 유효 범위를 벗어나면 CoreException이 발생한다.")
        @ValueSource(ints = {0, -1, 6})
        @ParameterizedTest
        void throwException_withInvalidRating(int rating) {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            // when & then
            assertThatThrownBy(() -> review.update(rating, "수정된 내용"))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.REVIEW_INVALID_RATING);
        }

        @DisplayName("수정 시 리뷰 내용이 비어있으면 CoreException이 발생한다.")
        @ValueSource(strings = {"", " "})
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidContent(String content) {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            // when & then
            assertThatThrownBy(() -> review.update(3, content))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("리뷰 작성자 검증")
    @Nested
    class ValidateOwner {

        @DisplayName("작성자 본인이면 예외가 발생하지 않는다.")
        @Test
        void success_whenOwner() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            // when & then (예외 없음)
            review.validateOwner(1L);
        }

        @DisplayName("작성자가 아니면 CoreException이 발생한다.")
        @Test
        void throwException_whenNotOwner() {
            // given
            Review review = Review.create(1L, 1L, 1L, 5, "좋은 상품입니다.");

            // when & then
            assertThatThrownBy(() -> review.validateOwner(999L))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.REVIEW_NOT_OWNER);
        }
    }
}
