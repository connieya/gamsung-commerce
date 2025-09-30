package com.loopers.infrastructure;

import com.loopers.domain.ProductMetricsWeekly;
import com.loopers.domain.WeeklyScoreRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsWeeklyJpaRepository extends JpaRepository<ProductMetricsWeekly ,Long> {

    @Query(
            value = """
                    SELECT
                      pm.ref_product_id           AS productId,
                      pm.like_count               AS likeSum,
                      pm.sale_quantity            AS orderSum,
                      pm.view_count               AS viewSum,
                      (:wLike*pm.like_count + :wOrder*pm.sale_quantity + :wView*pm.view_count) AS score
                    FROM product_metrics_weekly pm
                    WHERE pm.week_start = :weekStart
                    ORDER BY score DESC, pm.sale_quantity DESC, pm.view_count DESC, pm.ref_product_id ASC
                    LIMIT 100
                    """,
            nativeQuery = true
    )
    List<WeeklyScoreRow> findTop100ByWeekStartWithRank(LocalDate weekStart, BigDecimal wLike, BigDecimal wOrder, BigDecimal wView);
}
