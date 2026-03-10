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
        int inserted = likeRepository.saveIfAbsent(userId, targetId, targetType);

        if (inserted > 0) {
            LikeTarget target = LikeTarget.create(targetId, targetType);
            likeSummaryRepository.increaseLikeCount(target);
            applicationEventPublisher.publishEvent(LikeEvent.Add.of(targetId, targetType));
        }

        return LikeInfo.of(targetType, targetId, getCount(targetId, targetType));
    }

    @Transactional
    public LikeInfo remove(Long userId, Long targetId, LikeTargetType targetType) {
        int deleted = likeRepository.delete(userId, targetId, targetType);

        if (deleted > 0) {
            LikeTarget target = LikeTarget.create(targetId, targetType);
            likeSummaryRepository.decreaseLikeCount(target);
            applicationEventPublisher.publishEvent(LikeEvent.Remove.of(targetId, targetType));
        }

        return LikeInfo.of(targetType, targetId, getCount(targetId, targetType));
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
