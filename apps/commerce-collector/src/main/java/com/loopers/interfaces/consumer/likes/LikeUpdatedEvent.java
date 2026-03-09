package com.loopers.interfaces.consumer.likes;

import com.loopers.domain.likes.LikeTargetType;
import com.loopers.domain.likes.LikeUpdateType;

public record LikeUpdatedEvent(
        Long targetId,
        LikeTargetType targetType,
        LikeUpdateType updateType // "INCREMENT" or "DECREMENT"
) {
}
