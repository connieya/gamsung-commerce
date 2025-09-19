package com.loopers.infrastructure;

import com.loopers.domain.MvProductRankMonthly;
import com.loopers.domain.MvProductRankRepository;
import com.loopers.domain.MvProductRankWeekly;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MvProductRankCoreRepository implements MvProductRankRepository {

    private final MvProductRankWeeklyJpaRepository mvProductRankWeeklyJpaRepository;
    private final MvProductRankMonthlyJpaRepository mvProductRankMonthlyJpaRepository;

    @Override
    public void deleteByWeekStart(LocalDate weekStart) {
        mvProductRankWeeklyJpaRepository.deleteByWeekStart(weekStart);
    }

    @Override
    public void saveProductRankWeeklyAll(List<MvProductRankWeekly> mvProductRankWeeklyList) {
        mvProductRankWeeklyJpaRepository.saveAll(mvProductRankWeeklyList);
    }

    @Override
    public void deleteByMonthStart(LocalDate monthStart) {
        mvProductRankMonthlyJpaRepository.deleteByMonthStart(monthStart);

    }

    @Override
    public void saveProductRankMonthlyAll(List<MvProductRankMonthly> mvProductRankMonthlyList) {
        mvProductRankMonthlyJpaRepository.saveAll(mvProductRankMonthlyList);
    }
}
