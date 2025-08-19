package com.loopers.domain.likes;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "product_like",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ref_user_id", "ref_product_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike extends BaseEntity {

    @Column(name = "ref_user_id", nullable = false)
    private Long userId;

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Builder
    private ProductLike(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public static ProductLike create(Long userId, Long productId) {
        return ProductLike
                .builder()
                .userId(userId)
                .productId(productId)
                .build();
    }

}
