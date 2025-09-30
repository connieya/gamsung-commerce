package com.loopers.domain.metrics;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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

    @Builder
    private ProductMetricsMonth(Long productId, Long likeCount, Long saleQuantity, Long viewCount, LocalDate monthStart) {
        this.productId = productId;
        this.likeCount = likeCount;
        this.saleQuantity = saleQuantity;
        this.viewCount = viewCount;
        this.monthStart = monthStart;
    }

    public static ProductMetricsMonth fromDaily(ProductMetrics productMetrics) {
        return ProductMetricsMonth
                .builder()
                .productId(productMetrics.getProductId())
                .likeCount(productMetrics.getLikeCount())
                .saleQuantity(productMetrics.getSaleQuantity())
                .viewCount(productMetrics.getViewCount())
                .monthStart(productMetrics.getDate().with(TemporalAdjusters.firstDayOfMonth()))
                .build();
    }

    public void aggregate(ProductMetrics productMetrics) {
        this.likeCount += productMetrics.getLikeCount();
        this.viewCount += productMetrics.getViewCount();
        this.saleQuantity += productMetrics.getSaleQuantity();
    }
}
