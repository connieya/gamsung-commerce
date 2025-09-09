package com.loopers.interfaces.consumer.likes;


import com.loopers.domain.likes.LikeUpdateType;

public record LikeUpdatedEvent(
        Long productId,
        LikeUpdateType updateType // "INCREMENT" or "DECREMENT"
) {
}

