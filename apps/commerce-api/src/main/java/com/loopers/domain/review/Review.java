package com.loopers.domain.review;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String content;

    @Builder
    private Review(Long userId, Long productId, Long orderId, int rating, String content) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID가 필요합니다.");
        }
        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID가 필요합니다.");
        }
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID가 필요합니다.");
        }
        if (rating < 1 || rating > 5) {
            throw new CoreException(ErrorType.REVIEW_INVALID_RATING);
        }
        if (!StringUtils.hasText(content)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "리뷰 내용이 필요합니다.");
        }

        this.userId = userId;
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.content = content;
    }

    public static Review create(Long userId, Long productId, Long orderId, int rating, String content) {
        return Review.builder()
                .userId(userId)
                .productId(productId)
                .orderId(orderId)
                .rating(rating)
                .content(content)
                .build();
    }

    public void update(int rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new CoreException(ErrorType.REVIEW_INVALID_RATING);
        }
        if (!StringUtils.hasText(content)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "리뷰 내용이 필요합니다.");
        }
        this.rating = rating;
        this.content = content;
    }

    public void validateOwner(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new CoreException(ErrorType.REVIEW_NOT_OWNER);
        }
    }
}
