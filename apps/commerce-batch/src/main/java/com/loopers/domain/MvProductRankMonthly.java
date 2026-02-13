package com.loopers.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "mv_product_rank_monthly",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_month_product", columnNames = {"month_start","ref_product_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MvProductRankMonthly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month_start", nullable = false)
    private LocalDate monthStart;

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
    private MvProductRankMonthly(LocalDate monthStart, Long productId,
                                 long likeSum, long orderSum, long viewSum,
                                 BigDecimal score, int rank) {
        this.monthStart = monthStart;
        this.productId = productId;
        this.likeSum = likeSum;
        this.orderSum = orderSum;
        this.viewSum = viewSum;
        this.score = score;
        this.rank = rank;
    }
}
