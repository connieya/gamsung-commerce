package com.loopers.domain;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public enum RankingWeight {
    VIEW(0.1),
    LIKE(0.2),
    SALE(0.7);

    private final BigDecimal weight;

    RankingWeight(double weight) {
        this.weight = BigDecimal.valueOf(weight); // double 오차를 문자열로 보정해 생성
    }
}
