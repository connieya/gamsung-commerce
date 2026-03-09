package com.loopers.domain.review;

public class ReviewCommand {

    public record Create(
            Long userId,
            Long productId,
            Long orderId,
            int rating,
            String content
    ) {}

    public record Update(
            Long reviewId,
            Long userId,
            int rating,
            String content
    ) {}

    public record Delete(
            Long reviewId,
            Long userId
    ) {}
}
