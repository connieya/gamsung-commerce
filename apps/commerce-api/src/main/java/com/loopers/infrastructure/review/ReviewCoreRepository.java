package com.loopers.infrastructure.review;

import com.loopers.domain.review.Review;
import com.loopers.domain.review.ReviewRatingInfo;
import com.loopers.domain.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewCoreRepository implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return reviewJpaRepository.findById(reviewId);
    }

    @Override
    public Page<Review> findByProductId(Long productId, Pageable pageable) {
        return reviewJpaRepository.findByProductIdAndDeletedAtIsNull(productId, pageable);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return reviewJpaRepository.existsByUserIdAndProductIdAndDeletedAtIsNull(userId, productId);
    }

    @Override
    public ReviewRatingInfo getAverageRatingByProductId(Long productId) {
        ReviewJpaRepository.RatingProjection projection = reviewJpaRepository.getAverageRatingAndCountByProductId(productId);
        if (projection == null) {
            return ReviewRatingInfo.empty();
        }
        return ReviewRatingInfo.of(projection.getAverageRating(), projection.getTotalCount());
    }
}
