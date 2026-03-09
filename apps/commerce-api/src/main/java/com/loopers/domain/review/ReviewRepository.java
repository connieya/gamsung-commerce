package com.loopers.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long reviewId);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    ReviewRatingInfo getAverageRatingByProductId(Long productId);
}
