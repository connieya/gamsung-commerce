package com.loopers.domain.like;

import com.loopers.domain.like.exception.LikeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class LikeTest {

    @Nested
    @DisplayName("Like 생성")
    class CreateLike {

        @Test
        @DisplayName("userId, targetId, targetType으로 생성된다")
        void create_success() {
            // given
            Long userId = 1L;
            Long targetId = 100L;
            LikeTargetType targetType = LikeTargetType.PRODUCT;

            // when
            Like like = Like.create(userId, targetId, targetType);

            // then
            assertAll(
                    () -> assertThat(like.getUserId()).isEqualTo(userId),
                    () -> assertThat(like.getTarget().getId()).isEqualTo(targetId),
                    () -> assertThat(like.getTarget().getType()).isEqualTo(targetType)
            );
        }
    }

    @Nested
    @DisplayName("LikeTarget 생성")
    class CreateLikeTarget {

        @Test
        @DisplayName("id와 type으로 생성된다")
        void create_success() {
            // when
            LikeTarget target = LikeTarget.create(1L, LikeTargetType.PRODUCT);

            // then
            assertAll(
                    () -> assertThat(target.getId()).isEqualTo(1L),
                    () -> assertThat(target.getType()).isEqualTo(LikeTargetType.PRODUCT)
            );
        }

        @Test
        @DisplayName("id가 null이면 예외가 발생한다")
        void create_nullId_throwsException() {
            assertThatThrownBy(() -> LikeTarget.create(null, LikeTargetType.PRODUCT))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("type이 null이면 예외가 발생한다")
        void create_nullType_throwsException() {
            assertThatThrownBy(() -> LikeTarget.create(1L, null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("LikeSummary")
    class LikeSummaryTest {

        @Test
        @DisplayName("생성 시 likeCount는 0이다")
        void create_initialCount() {
            // when
            LikeSummary summary = LikeSummary.create(1L, LikeTargetType.PRODUCT);

            // then
            assertThat(summary.getLikeCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("increase 호출 시 likeCount가 1 증가한다")
        void increase() {
            // given
            LikeSummary summary = LikeSummary.create(1L, LikeTargetType.PRODUCT);

            // when
            summary.increase();

            // then
            assertThat(summary.getLikeCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("decrease 호출 시 likeCount가 1 감소한다")
        void decrease() {
            // given
            LikeSummary summary = LikeSummary.create(1L, LikeTargetType.PRODUCT);
            summary.increase();
            summary.increase();

            // when
            summary.decrease();

            // then
            assertThat(summary.getLikeCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("likeCount가 0일 때 decrease하면 예외가 발생한다")
        void decrease_whenZero_throwsException() {
            // given
            LikeSummary summary = LikeSummary.create(1L, LikeTargetType.PRODUCT);

            // when & then
            assertThatThrownBy(summary::decrease)
                    .isInstanceOf(LikeException.LikeCountCannotBeNegativeException.class);
        }
    }
}
