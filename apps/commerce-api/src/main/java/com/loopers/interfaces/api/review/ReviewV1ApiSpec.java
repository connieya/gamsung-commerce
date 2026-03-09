package com.loopers.interfaces.api.review;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Review V1 API", description = "리뷰 관련 API 입니다.")
public interface ReviewV1ApiSpec {

    @Operation(summary = "리뷰 작성", description = "구매 확정된 상품에 리뷰를 작성합니다.")
    ApiResponse<ReviewV1Dto.Response.Review> createReview(
            @RequestParam("userId") Long userId,
            @RequestBody ReviewV1Dto.Request.Create request
    );

    @Operation(summary = "리뷰 단건 조회", description = "리뷰 ID로 리뷰를 조회합니다.")
    ApiResponse<ReviewV1Dto.Response.Review> getReview(
            @PathVariable("reviewId") Long reviewId
    );

    @Operation(summary = "상품 리뷰 목록 조회", description = "상품 ID로 리뷰 목록을 조회합니다.")
    ApiResponse<ReviewV1Dto.Response.ReviewList> getProductReviews(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    ApiResponse<ReviewV1Dto.Response.Review> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam("userId") Long userId,
            @RequestBody ReviewV1Dto.Request.Update request
    );

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    ApiResponse<Object> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam("userId") Long userId
    );

    @Operation(summary = "상품 평점 조회", description = "상품의 평균 평점과 리뷰 수를 조회합니다.")
    ApiResponse<ReviewV1Dto.Response.RatingSummary> getProductRating(
            @PathVariable("productId") Long productId
    );
}
