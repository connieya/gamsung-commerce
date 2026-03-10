package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class LikeCoreRepository implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public int saveIfAbsent(Long userId, Long targetId, LikeTargetType targetType) {
        return likeJpaRepository.insertIfAbsent(userId, targetId, targetType.name());
    }

    @Override
    public boolean exists(Long userId, Long targetId, LikeTargetType targetType) {
        return likeJpaRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }

    @Override
    public int delete(Long userId, Long targetId, LikeTargetType targetType) {
        return likeJpaRepository.deleteByUserAndTarget(userId, targetId, targetType);
    }

    @Override
    public List<Like> findByUserIdAndTargetType(Long userId, LikeTargetType targetType) {
        return likeJpaRepository.findByUserIdAndTargetType(userId, targetType);
    }

    @Override
    public List<Long> findTargetIdsByUserIdAndTargetType(Long userId, LikeTargetType targetType) {
        return likeJpaRepository.findTargetIdsByUserIdAndTargetType(userId, targetType);
    }
}
