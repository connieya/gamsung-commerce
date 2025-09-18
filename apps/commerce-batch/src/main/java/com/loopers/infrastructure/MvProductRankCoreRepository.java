package com.loopers.infrastructure;

import com.loopers.domain.MvProductRankRepository;
import com.loopers.domain.MvProductRankWeekly;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MvProductRankCoreRepository implements MvProductRankRepository {

    private final MvProductRankJpaRepository mvProductRankJpaRepository;

    @Override
    public void deleteByWeekStart(LocalDate weekStart) {
        mvProductRankJpaRepository.deleteByWeekStart(weekStart);
    }

    @Override
    public void saveAll(List<MvProductRankWeekly> mvProductRankWeeklies) {
        mvProductRankJpaRepository.saveAll(mvProductRankWeeklies);
    }
}
