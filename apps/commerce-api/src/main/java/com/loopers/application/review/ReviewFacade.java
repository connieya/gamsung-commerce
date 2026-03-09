package com.loopers.application.review;

import com.loopers.domain.review.ReviewCommand;
import com.loopers.domain.review.ReviewInfo;
import com.loopers.domain.review.ReviewRatingInfo;
import com.loopers.domain.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewService reviewService;
    private final OrderVerifier orderVerifier;

    public ReviewInfo createReview(ReviewCriteria.Create criteria) {
        orderVerifier.verifyPurchase(criteria.orderId(), criteria.userId(), criteria.productId());

        ReviewCommand.Create command = new ReviewCommand.Create(
                criteria.userId(),
                criteria.productId(),
                criteria.orderId(),
                criteria.rating(),
                criteria.content()
        );
        return reviewService.create(command);
    }

    public ReviewInfo getReview(Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    public ReviewResult.ReviewList getProductReviews(ReviewCriteria.GetByProduct criteria) {
        Page<ReviewInfo> reviews = reviewService.getReviewsByProductId(
                criteria.productId(), criteria.page(), criteria.size()
        );
        ReviewRatingInfo ratingInfo = reviewService.getRatingInfo(criteria.productId());
        return ReviewResult.ReviewList.of(reviews, ratingInfo);
    }

    public ReviewInfo updateReview(ReviewCriteria.Update criteria) {
        ReviewCommand.Update command = new ReviewCommand.Update(
                criteria.reviewId(), criteria.userId(), criteria.rating(), criteria.content()
        );
        return reviewService.update(command);
    }

    public void deleteReview(ReviewCriteria.Delete criteria) {
        ReviewCommand.Delete command = new ReviewCommand.Delete(criteria.reviewId(), criteria.userId());
        reviewService.delete(command);
    }

    public ReviewRatingInfo getProductRating(Long productId) {
        return reviewService.getRatingInfo(productId);
    }
}
