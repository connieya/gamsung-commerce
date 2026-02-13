package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.MetricRepository;
import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsMonth;
import com.loopers.domain.metrics.ProductMetricsWeekly;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MetricCoreRepository implements MetricRepository {

    private final MetricJpaRepository metricJpaRepository;
    private final MetricWeeklyJpaRepository metricWeeklyJpaRepository;
    private final MetricMonthJpaRepository metricMonthJpaRepository;

    @Override
    public int upsert(ProductMetrics productMetrics) {
        productMetrics.prePersist();
        return metricJpaRepository.upsert(
                productMetrics.getDate()
                ,productMetrics.getLikeCount()
                ,productMetrics.getSaleQuantity()
                ,productMetrics.getViewCount()
                ,productMetrics.getProductId()
                ,productMetrics.getCreatedAt(),
                productMetrics.getUpdatedAt()
        );
    }

    @Override
    public Optional<ProductMetricsWeekly> findByProductIdAndWeekStart(Long productId, LocalDate date) {
        return metricWeeklyJpaRepository.findByProductIdAndWeekStart(productId, date);
    }

    @Override
    public ProductMetricsWeekly save(ProductMetricsWeekly productMetricsWeekly) {
        return metricWeeklyJpaRepository.save(productMetricsWeekly);
    }

    @Override
    public Optional<ProductMetricsMonth> findByProductIdAndMonthStart(Long productId, LocalDate date) {
        return metricMonthJpaRepository.findByProductIdAndMonthStart(productId, date);
    }

    @Override
    public ProductMetricsMonth save(ProductMetricsMonth productMetricsMonth) {
        return metricMonthJpaRepository.save(productMetricsMonth);
    }
}
