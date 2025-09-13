package com.loopers.domain.metrics;

import lombok.Getter;

@Getter
public enum RankingWeight {
    VIEW(0.1) , LIKE(0.2) , SALE(0.7);

    private final Double weight;


    RankingWeight(Double weight) {
        this.weight = weight;
    }
}
