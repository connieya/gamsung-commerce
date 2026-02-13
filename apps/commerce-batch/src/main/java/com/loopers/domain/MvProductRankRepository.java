package com.loopers.domain;

import java.time.LocalDate;
import java.util.List;

public interface MvProductRankRepository {
    void deleteByWeekStart(LocalDate weekStart);

    void saveProductRankWeeklyAll(List<MvProductRankWeekly> mvProductRankWeeklyList);

    void deleteByMonthStart(LocalDate monthStart);

    void saveProductRankMonthlyAll(List<MvProductRankMonthly> mvProductRankMonthlyList);

}
