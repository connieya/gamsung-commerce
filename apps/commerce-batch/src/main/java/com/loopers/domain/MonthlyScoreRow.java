package com.loopers.domain;

import java.math.BigDecimal;

public interface MonthlyScoreRow {
    Long getProductId();
    long getLikeSum();
    long getOrderSum();
    long getViewSum();
    BigDecimal getScore();
}
