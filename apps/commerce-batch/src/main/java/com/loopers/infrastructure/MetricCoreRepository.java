package com.loopers.infrastructure;

import com.loopers.domain.MetricRepository;
import com.loopers.domain.WeeklyScoreRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MetricCoreRepository implements MetricRepository {

    private final ProductMetricsWeeklyJpaRepository productMetricsWeeklyJpaRepository;


    @Override
    public List<WeeklyScoreRow> findTop100ByWeekStartWithRank(LocalDate weekStart, BigDecimal wLike, BigDecimal wOrder, BigDecimal wView) {
        return productMetricsWeeklyJpaRepository.findTop100ByWeekStartWithRank(weekStart,wLike,wOrder,wView);
    }
}
