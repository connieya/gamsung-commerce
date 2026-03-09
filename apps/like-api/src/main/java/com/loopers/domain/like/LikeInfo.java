package com.loopers.domain.like;

public record LikeInfo(
        LikeTargetType targetType,
        Long targetId,
        Long count
) {
    public static LikeInfo of(LikeTargetType targetType, Long targetId, Long count) {
        return new LikeInfo(targetType, targetId, count);
    }
}
