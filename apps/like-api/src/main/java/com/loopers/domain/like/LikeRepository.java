package com.loopers.domain.like;

import java.util.List;

public interface LikeRepository {

    Like save(Long userId, Long targetId, LikeTargetType targetType);

    boolean exists(Long userId, Long targetId, LikeTargetType targetType);

    void delete(Long userId, Long targetId, LikeTargetType targetType);

    List<Like> findByUserIdAndTargetType(Long userId, LikeTargetType targetType);

    List<Long> findTargetIdsByUserIdAndTargetType(Long userId, LikeTargetType targetType);
}
