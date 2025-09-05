package com.loopers.domain.likes.event;

import java.util.UUID;

public class ProductLikeEvent {

    public record Update(
            String eventKey,
            String eventName,
            Long productId,
            UpdateType updateType // "INCREMENT" or "DECREMENT"
    ) {
        public enum UpdateType {INCREMENT, DECREMENT}

        public static Update of(Long productId, UpdateType updateType) {
            return new Update(UUID.randomUUID().toString(), "like-update", productId, updateType);
        }
    }
}
