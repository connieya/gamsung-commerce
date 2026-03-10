package com.loopers.domain.like;

import com.loopers.domain.like.event.LikeEvent;
import com.loopers.infrastructure.like.LikeSummaryGapLockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gap Lock Deadlock 재현 테스트 전용 서비스.
 * findByTargetForUpdate + orElseGet(save) 패턴이 왜 Deadlock을 유발하는지 증명하기 위한 원래 구현.
 * 기존 LikeService 코드를 변경하지 않고 독립적으로 존재한다.
 */
@Service
@RequiredArgsConstructor
class LikeServiceGapLock {

    private final LikeRepository likeRepository;
    private final LikeSummaryGapLockJpaRepository likeSummaryGapLockJpaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public LikeInfo add(Long userId, Long targetId, LikeTargetType targetType) {
        if (likeRepository.exists(userId, targetId, targetType)) {
            Long count = getCount(targetId, targetType);
            return LikeInfo.of(targetType, targetId, count);
        }

        likeRepository.saveIfAbsent(userId, targetId, targetType);

        // 원래 문제 패턴:
        // 행이 없을 때 SELECT FOR UPDATE → Gap Lock 획득
        // 이후 동시 트랜잭션이 INSERT 시도 → 순환 대기 → Deadlock (ERROR 1213)
        LikeSummary summary = likeSummaryGapLockJpaRepository
                .findByTargetForUpdate(LikeTarget.create(targetId, targetType))
                .orElseGet(() -> likeSummaryGapLockJpaRepository.save(LikeSummary.create(targetId, targetType)));
        summary.increase();

        applicationEventPublisher.publishEvent(LikeEvent.Add.of(targetId, targetType));
        return LikeInfo.of(targetType, targetId, summary.getLikeCount());
    }

    private Long getCount(Long targetId, LikeTargetType targetType) {
        return likeSummaryGapLockJpaRepository
                .findByTarget(LikeTarget.create(targetId, targetType))
                .map(LikeSummary::getLikeCount)
                .orElse(0L);
    }
}
