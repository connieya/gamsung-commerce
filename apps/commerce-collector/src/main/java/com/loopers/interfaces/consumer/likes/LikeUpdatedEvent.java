package com.loopers.interfaces.consumer.likes;



public record LikeUpdatedEvent(
        Long productId,
        UpdateType updateType // "INCREMENT" or "DECREMENT"
) {
    public enum UpdateType { INCREMENT, DECREMENT }
}

