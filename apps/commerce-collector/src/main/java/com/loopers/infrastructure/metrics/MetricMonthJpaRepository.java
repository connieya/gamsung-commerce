package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetricsMonth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MetricMonthJpaRepository extends JpaRepository<ProductMetricsMonth , Long> {
    Optional<ProductMetricsMonth> findByProductIdAndMonthStart(Long productId, LocalDate date);
}
