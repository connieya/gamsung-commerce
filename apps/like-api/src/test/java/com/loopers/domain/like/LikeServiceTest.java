package com.loopers.domain.like;

import com.loopers.domain.like.event.LikeEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    LikeService likeService;

    @Mock
    LikeRepository likeRepository;

    @Mock
    LikeSummaryRepository likeSummaryRepository;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Nested
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("새로운 좋아요를 추가하면 원자적으로 count가 증가하고 이벤트를 발행한다")
        void add_newLike() {
            // given
            Long userId = 1L;
            Long targetId = 100L;
            LikeTargetType targetType = LikeTargetType.PRODUCT;

            when(likeRepository.saveIfAbsent(userId, targetId, targetType)).thenReturn(1);

            LikeSummary updatedSummary = LikeSummary.create(targetId, targetType);
            updatedSummary.increase();

            when(likeSummaryRepository.findByTarget(any(LikeTarget.class)))
                    .thenReturn(Optional.of(updatedSummary));
            when(likeSummaryRepository.increaseLikeCount(any(LikeTarget.class))).thenReturn(1);

            // when
            LikeInfo result = likeService.add(userId, targetId, targetType);

            // then
            assertAll(
                    () -> assertThat(result.targetType()).isEqualTo(targetType),
                    () -> assertThat(result.targetId()).isEqualTo(targetId),
                    () -> assertThat(result.count()).isEqualTo(1L)
            );
            verify(likeRepository).saveIfAbsent(userId, targetId, targetType);
            verify(likeSummaryRepository).increaseLikeCount(any(LikeTarget.class));
            verify(applicationEventPublisher).publishEvent(any(LikeEvent.Add.class));
        }

        @Test
        @DisplayName("이미 좋아요한 경우 멱등하게 현재 count를 반환한다")
        void add_alreadyExists_idempotent() {
            // given
            Long userId = 1L;
            Long targetId = 100L;
            LikeTargetType targetType = LikeTargetType.PRODUCT;

            when(likeRepository.saveIfAbsent(userId, targetId, targetType)).thenReturn(0);

            LikeSummary summary = LikeSummary.create(targetId, targetType);
            summary.increase();
            when(likeSummaryRepository.findByTarget(any(LikeTarget.class)))
                    .thenReturn(Optional.of(summary));

            // when
            LikeInfo result = likeService.add(userId, targetId, targetType);

            // then
            assertAll(
                    () -> assertThat(result.count()).isEqualTo(1L),
                    () -> assertThat(result.targetId()).isEqualTo(targetId)
            );
            verify(likeSummaryRepository, never()).increaseLikeCount(any(LikeTarget.class));
            verify(applicationEventPublisher, never()).publishEvent(any());
        }

    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        @DisplayName("좋아요를 삭제하면 원자적으로 count가 감소하고 이벤트를 발행한다")
        void remove_existingLike() {
            // given
            Long userId = 1L;
            Long targetId = 100L;
            LikeTargetType targetType = LikeTargetType.PRODUCT;

            when(likeRepository.delete(userId, targetId, targetType)).thenReturn(1);

            LikeSummary updatedSummary = LikeSummary.create(targetId, targetType);
            updatedSummary.increase();

            when(likeSummaryRepository.findByTarget(any(LikeTarget.class)))
                    .thenReturn(Optional.of(updatedSummary));
            when(likeSummaryRepository.decreaseLikeCount(any(LikeTarget.class))).thenReturn(1);

            // when
            LikeInfo result = likeService.remove(userId, targetId, targetType);

            // then
            assertAll(
                    () -> assertThat(result.count()).isEqualTo(1L),
                    () -> assertThat(result.targetId()).isEqualTo(targetId)
            );
            verify(likeRepository).delete(userId, targetId, targetType);
            verify(likeSummaryRepository).decreaseLikeCount(any(LikeTarget.class));
            verify(applicationEventPublisher).publishEvent(any(LikeEvent.Remove.class));
        }

        @Test
        @DisplayName("좋아요하지 않은 경우 멱등하게 현재 count를 반환한다")
        void remove_notExists_idempotent() {
            // given
            Long userId = 1L;
            Long targetId = 100L;
            LikeTargetType targetType = LikeTargetType.PRODUCT;

            when(likeRepository.delete(userId, targetId, targetType)).thenReturn(0);
            when(likeSummaryRepository.findByTarget(any(LikeTarget.class)))
                    .thenReturn(Optional.empty());

            // when
            LikeInfo result = likeService.remove(userId, targetId, targetType);

            // then
            assertAll(
                    () -> assertThat(result.count()).isEqualTo(0L),
                    () -> assertThat(result.targetId()).isEqualTo(targetId)
            );
            verify(likeSummaryRepository, never()).decreaseLikeCount(any(LikeTarget.class));
            verify(applicationEventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("findByUserIdAndTargetType")
    class FindByUserIdAndTargetType {

        @Test
        @DisplayName("사용자의 특정 타입 좋아요 목록을 반환한다")
        void findByUserIdAndTargetType_success() {
            // given
            Long userId = 1L;
            LikeTargetType targetType = LikeTargetType.PRODUCT;
            List<Like> likes = List.of(
                    Like.create(userId, 100L, targetType),
                    Like.create(userId, 200L, targetType)
            );

            when(likeRepository.findByUserIdAndTargetType(userId, targetType)).thenReturn(likes);

            // when
            List<Like> result = likeService.findByUserIdAndTargetType(userId, targetType);

            // then
            assertThat(result).hasSize(2);
        }
    }
}
