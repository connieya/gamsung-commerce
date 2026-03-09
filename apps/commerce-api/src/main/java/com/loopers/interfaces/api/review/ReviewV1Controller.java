package com.loopers.interfaces.api.review;

import com.loopers.application.review.ReviewCriteria;
import com.loopers.application.review.ReviewFacade;
import com.loopers.application.review.ReviewResult;
import com.loopers.domain.review.ReviewInfo;
import com.loopers.domain.review.ReviewRatingInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewV1Controller implements ReviewV1ApiSpec {

    private final ReviewFacade reviewFacade;

    @PostMapping
    @Override
    public ApiResponse<ReviewV1Dto.Response.Review> createReview(
            @RequestParam("userId") Long userId,
            @RequestBody ReviewV1Dto.Request.Create request
    ) {
        ReviewCriteria.Create criteria = new ReviewCriteria.Create(
                userId,
                request.productId(),
                request.orderId(),
                request.rating(),
                request.content()
        );
        ReviewInfo reviewInfo = reviewFacade.createReview(criteria);
        return ApiResponse.success(ReviewV1Dto.Response.Review.from(reviewInfo));
    }

    @GetMapping("/{reviewId}")
    @Override
    public ApiResponse<ReviewV1Dto.Response.Review> getReview(
            @PathVariable("reviewId") Long reviewId
    ) {
        ReviewInfo reviewInfo = reviewFacade.getReview(reviewId);
        return ApiResponse.success(ReviewV1Dto.Response.Review.from(reviewInfo));
    }

    @GetMapping("/products/{productId}")
    @Override
    public ApiResponse<ReviewV1Dto.Response.ReviewList> getProductReviews(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        ReviewCriteria.GetByProduct criteria = new ReviewCriteria.GetByProduct(productId, page, size);
        ReviewResult.ReviewList result = reviewFacade.getProductReviews(criteria);
        return ApiResponse.success(ReviewV1Dto.Response.ReviewList.from(result));
    }

    @PutMapping("/{reviewId}")
    @Override
    public ApiResponse<ReviewV1Dto.Response.Review> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam("userId") Long userId,
            @RequestBody ReviewV1Dto.Request.Update request
    ) {
        ReviewCriteria.Update criteria = new ReviewCriteria.Update(
                reviewId, userId, request.rating(), request.content()
        );
        ReviewInfo reviewInfo = reviewFacade.updateReview(criteria);
        return ApiResponse.success(ReviewV1Dto.Response.Review.from(reviewInfo));
    }

    @DeleteMapping("/{reviewId}")
    @Override
    public ApiResponse<Object> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam("userId") Long userId
    ) {
        ReviewCriteria.Delete criteria = new ReviewCriteria.Delete(reviewId, userId);
        reviewFacade.deleteReview(criteria);
        return ApiResponse.success();
    }

    @GetMapping("/products/{productId}/rating")
    @Override
    public ApiResponse<ReviewV1Dto.Response.RatingSummary> getProductRating(
            @PathVariable("productId") Long productId
    ) {
        ReviewRatingInfo ratingInfo = reviewFacade.getProductRating(productId);
        return ApiResponse.success(ReviewV1Dto.Response.RatingSummary.from(ratingInfo));
    }
}
