package com.loopers.domain.like;

import com.loopers.domain.like.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeSummaryRepository likeSummaryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public LikeInfo add(Long userId, Long targetId, LikeTargetType targetType) {
        if (likeRepository.exists(userId, targetId, targetType)) {
            Long count = getCount(targetId, targetType);
            return LikeInfo.of(targetType, targetId, count);
        }

        likeRepository.save(userId, targetId, targetType);

        LikeSummary summary = likeSummaryRepository
                .findByTargetForUpdate(LikeTarget.create(targetId, targetType))
                .orElseGet(() -> likeSummaryRepository.save(LikeSummary.create(targetId, targetType)));
        summary.increase();

        applicationEventPublisher.publishEvent(LikeEvent.Add.of(targetId, targetType));
        return LikeInfo.of(targetType, targetId, summary.getLikeCount());
    }

    @Transactional
    public LikeInfo remove(Long userId, Long targetId, LikeTargetType targetType) {
        if (!likeRepository.exists(userId, targetId, targetType)) {
            Long count = getCount(targetId, targetType);
            return LikeInfo.of(targetType, targetId, count);
        }

        likeRepository.delete(userId, targetId, targetType);

        LikeSummary summary = likeSummaryRepository
                .findByTargetForUpdate(LikeTarget.create(targetId, targetType))
                .orElseGet(() -> likeSummaryRepository.save(LikeSummary.create(targetId, targetType)));
        summary.decrease();

        applicationEventPublisher.publishEvent(LikeEvent.Remove.of(targetId, targetType));
        return LikeInfo.of(targetType, targetId, summary.getLikeCount());
    }

    @Transactional(readOnly = true)
    public List<Like> findByUserIdAndTargetType(Long userId, LikeTargetType targetType) {
        return likeRepository.findByUserIdAndTargetType(userId, targetType);
    }

    @Transactional(readOnly = true)
    public List<Long> findTargetIdsByUserIdAndTargetType(Long userId, LikeTargetType targetType) {
        return likeRepository.findTargetIdsByUserIdAndTargetType(userId, targetType);
    }

    private Long getCount(Long targetId, LikeTargetType targetType) {
        return likeSummaryRepository
                .findByTarget(LikeTarget.create(targetId, targetType))
                .map(LikeSummary::getLikeCount)
                .orElse(0L);
    }
}
