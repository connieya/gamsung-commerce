package com.loopers.interfaces.api.review;

import com.loopers.application.review.ReviewResult;
import com.loopers.domain.common.PageInfo;
import com.loopers.domain.review.ReviewInfo;
import com.loopers.domain.review.ReviewRatingInfo;

import java.time.ZonedDateTime;
import java.util.List;

public class ReviewV1Dto {

    public static class Request {

        public record Create(
                Long productId,
                Long orderId,
                int rating,
                String content
        ) {}

        public record Update(
                int rating,
                String content
        ) {}
    }

    public static class Response {

        public record Review(
                Long reviewId,
                Long userId,
                Long productId,
                Long orderId,
                int rating,
                String content,
                ZonedDateTime createdAt,
                ZonedDateTime updatedAt
        ) {
            public static Review from(ReviewInfo info) {
                return new Review(
                        info.reviewId(),
                        info.userId(),
                        info.productId(),
                        info.orderId(),
                        info.rating(),
                        info.content(),
                        info.createdAt(),
                        info.updatedAt()
                );
            }
        }

        public record ReviewList(
                List<Review> reviews,
                PageInfo pageInfo,
                RatingSummary ratingSummary
        ) {
            public static ReviewList from(ReviewResult.ReviewList result) {
                List<Review> reviews = result.reviews().stream()
                        .map(Review::from)
                        .toList();
                RatingSummary ratingSummary = RatingSummary.from(result.ratingInfo());
                return new ReviewList(reviews, result.pageInfo(), ratingSummary);
            }
        }

        public record RatingSummary(
                double averageRating,
                long totalCount
        ) {
            public static RatingSummary from(ReviewRatingInfo ratingInfo) {
                return new RatingSummary(ratingInfo.averageRating(), ratingInfo.totalCount());
            }
        }
    }
}
