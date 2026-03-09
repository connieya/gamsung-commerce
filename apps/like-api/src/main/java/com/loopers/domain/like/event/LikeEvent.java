package com.loopers.domain.like.event;

import com.loopers.domain.like.LikeTargetType;

import java.util.UUID;

public class LikeEvent {

    public record Add(Long targetId, LikeTargetType targetType) {
        public static Add of(Long targetId, LikeTargetType targetType) {
            return new Add(targetId, targetType);
        }
    }

    public record Remove(Long targetId, LikeTargetType targetType) {
        public static Remove of(Long targetId, LikeTargetType targetType) {
            return new Remove(targetId, targetType);
        }
    }

    public record Update(
            String eventKey,
            String eventName,
            Long targetId,
            LikeTargetType targetType,
            UpdateType updateType
    ) {
        public enum UpdateType { INCREMENT, DECREMENT }

        public static Update of(Long targetId, LikeTargetType targetType, UpdateType updateType) {
            return new Update(UUID.randomUUID().toString(), "like-update", targetId, targetType, updateType);
        }
    }
}
