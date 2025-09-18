package com.loopers.domain.metrics;

import java.time.LocalDate;
import java.util.Optional;

public interface MetricRepository {

    int upsert(ProductMetrics productMetrics);

    Optional<ProductMetricsWeekly> findByProductIdAndWeekStart(Long productId, LocalDate date);

    ProductMetricsWeekly save(ProductMetricsWeekly productMetricsWeekly);

    Optional<ProductMetricsMonth> findByProductIdAndMonthStart(Long productId, LocalDate date);

    ProductMetricsMonth save(ProductMetricsMonth productMetricsMonth);

}
