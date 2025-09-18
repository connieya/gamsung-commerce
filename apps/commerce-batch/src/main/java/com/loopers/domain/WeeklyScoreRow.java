package com.loopers.domain;

import java.math.BigDecimal;

public interface WeeklyScoreRow {
    Long getProductId();
    long getLikeSum();
    long getOrderSum();
    long getViewSum();
    BigDecimal getScore();
}
