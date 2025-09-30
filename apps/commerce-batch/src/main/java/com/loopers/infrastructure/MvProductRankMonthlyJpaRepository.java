package com.loopers.infrastructure;

import com.loopers.domain.MvProductRankMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface MvProductRankMonthlyJpaRepository extends JpaRepository<MvProductRankMonthly ,Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MvProductRankMonthly m WHERE m.monthStart = :monthStart")
    void deleteByMonthStart(LocalDate monthStart);
}
