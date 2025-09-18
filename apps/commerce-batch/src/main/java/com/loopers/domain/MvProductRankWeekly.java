package com.loopers.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(
        name = "mv_product_rank_weekly",
        uniqueConstraints = @UniqueConstraint(name = "uk_week_product", columnNames = {"week_start","ref_product_id"})
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MvProductRankWeekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Column(name = "like_sum", nullable = false)
    private long likeSum;

    @Column(name = "order_sum", nullable = false)
    private long orderSum;

    @Column(name = "view_sum", nullable = false)
    private long viewSum;

    @Column(name = "score", nullable = false, precision = 18, scale = 6)
    private BigDecimal score;

    @Column(name = "`rank`", nullable = false)
    private int rank;

    @Builder
    private MvProductRankWeekly(LocalDate weekStart, Long productId,
                                long likeSum, long orderSum, long viewSum,
                                BigDecimal score, int rank) {
        this.weekStart = weekStart;
        this.productId = productId;
        this.likeSum = likeSum;
        this.orderSum = orderSum;
        this.viewSum = viewSum;
        this.score = score;
        this.rank = rank;
    }
}
