package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetricsWeekly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MetricWeeklyJpaRepository extends JpaRepository<ProductMetricsWeekly , Long> {
    Optional<ProductMetricsWeekly> findByProductIdAndWeekStart(Long productId, LocalDate date);
}
