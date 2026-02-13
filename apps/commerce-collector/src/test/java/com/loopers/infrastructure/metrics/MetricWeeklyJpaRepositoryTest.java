package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetricsWeekly;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MetricWeeklyJpaRepositoryTest {

    @Autowired
    private  MetricWeeklyJpaRepository metricWeeklyJpaRepository;


    @Test
    void test() {
        ProductMetricsWeekly productMetricsWeekly = ProductMetricsWeekly
                .builder()
                .productId(1L)
                .viewCount(1L)
                .saleQuantity(2L)
                .likeCount(3L)
                .weekStart(LocalDate.now())
                .build();

        metricWeeklyJpaRepository.save(productMetricsWeekly);

        Optional<ProductMetricsWeekly> byProductIdAndWeekStart = metricWeeklyJpaRepository.findByProductIdAndWeekStart(1L, LocalDate.now());

        assertThat(byProductIdAndWeekStart).isPresent();
        assertThat(byProductIdAndWeekStart.get().getLikeCount()).isEqualTo(3L);
    }

}
