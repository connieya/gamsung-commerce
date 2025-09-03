package com.loopers.domain.likes.event;

import com.loopers.domain.likes.LikeTargetType;

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
}
