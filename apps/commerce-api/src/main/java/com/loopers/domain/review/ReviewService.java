package com.loopers.domain.review;

import com.loopers.domain.review.exception.ReviewException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewInfo create(ReviewCommand.Create command) {
        if (reviewRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            throw new ReviewException.ReviewAlreadyExistsException(ErrorType.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.create(
                command.userId(),
                command.productId(),
                command.orderId(),
                command.rating(),
                command.content()
        );
        Review saved = reviewRepository.save(review);
        return ReviewInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public ReviewInfo getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException.ReviewNotFoundException(ErrorType.REVIEW_NOT_FOUND));
        return ReviewInfo.from(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewInfo> getReviewsByProductId(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewInfo::from);
    }

    @Transactional
    public ReviewInfo update(ReviewCommand.Update command) {
        Review review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new ReviewException.ReviewNotFoundException(ErrorType.REVIEW_NOT_FOUND));
        review.validateOwner(command.userId());
        review.update(command.rating(), command.content());
        return ReviewInfo.from(review);
    }

    @Transactional
    public void delete(ReviewCommand.Delete command) {
        Review review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new ReviewException.ReviewNotFoundException(ErrorType.REVIEW_NOT_FOUND));
        review.validateOwner(command.userId());
        review.delete();
    }

    @Transactional(readOnly = true)
    public ReviewRatingInfo getRatingInfo(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }
}
