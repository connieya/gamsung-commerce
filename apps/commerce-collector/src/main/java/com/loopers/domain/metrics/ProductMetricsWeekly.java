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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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

    @Builder
    private ProductMetricsWeekly(Long productId, Long likeCount, Long saleQuantity, Long viewCount, LocalDate weekStart) {
        this.productId = productId;
        this.likeCount = likeCount;
        this.saleQuantity = saleQuantity;
        this.viewCount = viewCount;
        this.weekStart = weekStart;
    }

    public static ProductMetricsWeekly fromDaily(ProductMetrics productMetrics) {
        return ProductMetricsWeekly.
                builder()
                .productId(productMetrics.getProductId())
                .likeCount(productMetrics.getLikeCount())
                .saleQuantity(productMetrics.getSaleQuantity())
                .viewCount(productMetrics.getViewCount())
                .weekStart(productMetrics.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                .build();
    }

    public void aggregate(ProductMetrics productMetrics) {
        this.likeCount += productMetrics.getLikeCount();
        this.saleQuantity += productMetrics.getSaleQuantity();
        this.viewCount += productMetrics.getViewCount();
    }


}
