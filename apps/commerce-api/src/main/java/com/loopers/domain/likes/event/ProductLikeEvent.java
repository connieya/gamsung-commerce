package com.loopers.domain.likes.event;

import com.loopers.domain.common.DomainEvent;

import java.util.UUID;

public class ProductLikeEvent {

    public record Update(
            String eventKey,
            String eventName,
            Long productId,
            UpdateType updateType // "INCREMENT" or "DECREMENT"
    ) implements DomainEvent {
        @Override
        public Long domainId() {
            return productId;
        }

        public enum UpdateType {INCREMENT, DECREMENT}

        public static Update of(Long productId, UpdateType updateType) {
            return new Update(UUID.randomUUID().toString(), "like-update", productId, updateType);
        }
    }
}
