package com.loopers.infrastructure.review;

import com.loopers.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndProductIdAndDeletedAtIsNull(Long userId, Long productId);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    Page<Review> findByProductIdAndDeletedAtIsNull(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0) AS averageRating, COUNT(r) AS totalCount FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    RatingProjection getAverageRatingAndCountByProductId(@Param("productId") Long productId);

    interface RatingProjection {
        Double getAverageRating();
        Long getTotalCount();
    }
}
