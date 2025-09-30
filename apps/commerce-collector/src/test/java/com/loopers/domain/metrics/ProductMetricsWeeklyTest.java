package com.loopers.domain.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductMetricsWeeklyTest {

    @Test
    @DisplayName("일 단위 지표가 주중 날짜일 경우, 주간 시작일은 해당 주의 월요일로 설정된다")
    void fromDailyWeeklyMetrics_fromMidweek_setsWeekStartToMonday() {
        ProductMetrics productMetrics = ProductMetrics
                .builder()
                .productId(1L)
                .likeCount(10L)
                .saleQuantity(20L)
                .viewCount(10L)
                .date(LocalDate.of(2025, 9, 18))
                .build();

        ProductMetricsWeekly productMetricsWeekly = ProductMetricsWeekly.fromDaily(productMetrics);

        assertAll(
                () -> assertThat(productMetricsWeekly.getProductId()).isEqualTo(1L),
                () -> assertThat(productMetricsWeekly.getLikeCount()).isEqualTo(10L),
                () -> assertThat(productMetricsWeekly.getSaleQuantity()).isEqualTo(20L),
                () -> assertThat(productMetricsWeekly.getViewCount()).isEqualTo(10L),
                () -> assertThat(productMetricsWeekly.getWeekStart()).isEqualTo(LocalDate.of(2025,9,15))
        );
    }


    @Test
    @DisplayName("일 단위 지표가 일요일일 경우, 주간 시작일은 이전 주의 월요일로 설정된다")
    void fromDailyWeeklyMetrics_fromSunday_setsWeekStartToMonday() {
        ProductMetrics productMetrics = ProductMetrics
                .builder()
                .productId(1L)
                .likeCount(10L)
                .saleQuantity(20L)
                .viewCount(10L)
                .date(LocalDate.of(2025, 9, 14))
                .build();

        ProductMetricsWeekly productMetricsWeekly = ProductMetricsWeekly.fromDaily(productMetrics);

        assertAll(
                () -> assertThat(productMetricsWeekly.getProductId()).isEqualTo(1L),
                () -> assertThat(productMetricsWeekly.getLikeCount()).isEqualTo(10L),
                () -> assertThat(productMetricsWeekly.getSaleQuantity()).isEqualTo(20L),
                () -> assertThat(productMetricsWeekly.getViewCount()).isEqualTo(10L),
                () -> assertThat(productMetricsWeekly.getWeekStart()).isEqualTo(LocalDate.of(2025,9,8))
        );
    }
}
