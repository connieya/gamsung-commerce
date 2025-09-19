package com.loopers.infrastructure;

import com.loopers.domain.MvProductRankWeekly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface MvProductRankWeeklyJpaRepository extends JpaRepository<MvProductRankWeekly ,Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MvProductRankWeekly m WHERE m.weekStart = :weekStart")
    void deleteByWeekStart(LocalDate weekStart);
}
