package com.loopers.domain.metrics;

public interface MetricRepository {

    int upsert(ProductMetrics productMetrics);
}
