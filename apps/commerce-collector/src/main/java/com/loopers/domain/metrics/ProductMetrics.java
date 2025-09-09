package com.loopers.domain.metrics;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "product_metrics")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductMetrics extends BaseEntity {

    @Column(name = "metric_date", nullable = false)
    private LocalDate date;

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "sale_quantity", nullable = false)
    private Long saleQuantity;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Builder
    public ProductMetrics(LocalDate date, Long productId, Long likeCount, Long saleQuantity, Long viewCount) {
        this.date = date;
        this.productId = productId;
        this.likeCount = likeCount;
        this.saleQuantity = saleQuantity;
        this.viewCount = viewCount;
    }

}
