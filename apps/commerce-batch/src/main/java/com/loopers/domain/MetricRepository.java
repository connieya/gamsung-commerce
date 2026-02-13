package com.loopers.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MetricRepository {
    List<WeeklyScoreRow> findTop100ByWeekStartWithRank(LocalDate weekStart, BigDecimal wLike, BigDecimal wOrder, BigDecimal wView);

    List<MonthlyScoreRow> findTop100ByMonthStartWithRank(LocalDate monthStart, BigDecimal wLike, BigDecimal wOrder, BigDecimal wView);
}
