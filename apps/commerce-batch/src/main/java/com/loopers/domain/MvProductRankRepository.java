package com.loopers.domain;

import java.time.LocalDate;
import java.util.List;

public interface MvProductRankRepository {
    void deleteByWeekStart(LocalDate weekStart);

    void saveAll(List<MvProductRankWeekly> mvProductRankWeeklies);
}
