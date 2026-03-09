package com.loopers.domain.review;

public record ReviewRatingInfo(
        double averageRating,
        long totalCount
) {
    public static ReviewRatingInfo empty() {
        return new ReviewRatingInfo(0.0, 0);
    }

    public static ReviewRatingInfo of(double averageRating, long totalCount) {
        return new ReviewRatingInfo(averageRating, totalCount);
    }
}
