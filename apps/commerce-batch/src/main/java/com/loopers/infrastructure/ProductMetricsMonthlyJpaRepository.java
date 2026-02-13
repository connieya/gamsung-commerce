package com.loopers.infrastructure;

import com.loopers.domain.MonthlyScoreRow;
import com.loopers.domain.ProductMetricsMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsMonthlyJpaRepository extends JpaRepository<ProductMetricsMonth ,Long> {
    @Query(value = """
    SELECT
      pm.ref_product_id AS productId,
      pm.like_count     AS likeSum,
      pm.sale_quantity  AS orderSum,
      pm.view_count     AS viewSum,
      (:wLike*pm.like_count + :wOrder*pm.sale_quantity + :wView*pm.view_count) AS score
    FROM product_metrics_monthly pm
    WHERE pm.month_start = :monthStart
    ORDER BY score DESC, orderSum DESC, viewSum DESC, productId ASC
    LIMIT 100
    """, nativeQuery = true)
    List<MonthlyScoreRow> findTop100ByMonthStartWithRank(LocalDate monthStart, BigDecimal wLike, BigDecimal wOrder, BigDecimal wView);
}
