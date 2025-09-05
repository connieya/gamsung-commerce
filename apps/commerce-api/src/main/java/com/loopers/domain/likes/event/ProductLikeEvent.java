package com.loopers.domain.likes.event;

import com.loopers.domain.common.DomainEvent;
import com.loopers.domain.likes.LikeTargetType;

import java.util.UUID;

public class ProductLikeEvent {

    public record Add(Long productId, LikeTargetType likeTargetType) {
        public static Add of(Long productId, LikeTargetType likeTargetType) {
            return new Add(productId, likeTargetType);
        }
    }

    public record Remove(Long productId, LikeTargetType likeTargetType) {
        public static Remove of(Long productId, LikeTargetType likeTargetType) {
            return new Remove(productId, likeTargetType);
        }
    }


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
