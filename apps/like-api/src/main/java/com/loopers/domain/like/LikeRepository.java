package com.loopers.domain.like;

import java.util.List;

public interface LikeRepository {

    int saveIfAbsent(Long userId, Long targetId, LikeTargetType targetType);

    boolean exists(Long userId, Long targetId, LikeTargetType targetType);

    int delete(Long userId, Long targetId, LikeTargetType targetType);

    List<Like> findByUserIdAndTargetType(Long userId, LikeTargetType targetType);

    List<Long> findTargetIdsByUserIdAndTargetType(Long userId, LikeTargetType targetType);
}
