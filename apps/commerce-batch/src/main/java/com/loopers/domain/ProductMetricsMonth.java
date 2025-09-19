package com.loopers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "product_metrics_monthly",
        uniqueConstraints = @UniqueConstraint(name = "uk_month_product", columnNames = {"month_start", "ref_product_Id"}))
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetricsMonth extends BaseEntity {

    @Column(name = "ref_product_id")
    private Long productId;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "sale_quantity")
    private Long saleQuantity;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "month_start")
    private LocalDate monthStart;
}
