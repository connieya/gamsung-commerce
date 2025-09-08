package com.loopers.domain.metrics;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Table(name = "product_metrics")
@Entity
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


}
