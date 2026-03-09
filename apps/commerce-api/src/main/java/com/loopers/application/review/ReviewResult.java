package com.loopers.application.review;

import com.loopers.domain.common.PageInfo;
import com.loopers.domain.review.ReviewInfo;
import com.loopers.domain.review.ReviewRatingInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public class ReviewResult {

    public record ReviewList(
            List<ReviewInfo> reviews,
            PageInfo pageInfo,
            ReviewRatingInfo ratingInfo
    ) {
        public static ReviewList of(Page<ReviewInfo> reviewPage, ReviewRatingInfo ratingInfo) {
            PageInfo pageInfo = PageInfo.create(
                    reviewPage.getNumber(),
                    reviewPage.getSize(),
                    reviewPage.getTotalPages(),
                    reviewPage.getTotalElements(),
                    reviewPage.hasNext()
            );
            return new ReviewList(reviewPage.getContent(), pageInfo, ratingInfo);
        }
    }
}
