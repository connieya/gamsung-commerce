package com.loopers.application.review;

public class ReviewCriteria {

    public record Create(
            Long userId,
            Long productId,
            Long orderId,
            int rating,
            String content
    ) {}

    public record GetByProduct(
            Long productId,
            int page,
            int size
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
