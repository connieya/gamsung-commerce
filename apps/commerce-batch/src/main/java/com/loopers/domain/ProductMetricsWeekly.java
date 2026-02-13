package com.loopers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "product_metrics_weekly",
        uniqueConstraints = @UniqueConstraint(name = "uk_week_product", columnNames = {"week_start", "ref_product_id"})
)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductMetricsWeekly extends BaseEntity {

    @Column(name = "ref_product_id")
    private Long productId;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "sale_quantity")
    private Long saleQuantity;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "week_start")
    private LocalDate weekStart;
}

