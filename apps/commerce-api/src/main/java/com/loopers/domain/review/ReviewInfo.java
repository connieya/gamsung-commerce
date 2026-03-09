package com.loopers.domain.review;

import java.time.ZonedDateTime;

public record ReviewInfo(
        Long reviewId,
        Long userId,
        Long productId,
        Long orderId,
        int rating,
        String content,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ReviewInfo from(Review review) {
        return new ReviewInfo(
                review.getId(),
                review.getUserId(),
                review.getProductId(),
                review.getOrderId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
