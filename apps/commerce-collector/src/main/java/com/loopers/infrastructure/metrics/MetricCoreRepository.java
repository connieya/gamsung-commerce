package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.MetricRepository;
import com.loopers.domain.metrics.ProductMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MetricCoreRepository implements MetricRepository {

    private final MetricJpaRepository metricJpaRepository;

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
}
