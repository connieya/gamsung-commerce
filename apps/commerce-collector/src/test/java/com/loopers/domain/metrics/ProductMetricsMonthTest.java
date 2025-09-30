package com.loopers.domain.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductMetricsMonthTest {


    @ParameterizedTest
    @CsvSource(textBlock = """
            2025-09-01 | 2025-09-01
            2025-09-02 | 2025-09-01
            2025-09-18 | 2025-09-01
            2025-09-30 | 2025-09-01
            2025-10-01 | 2025-10-01
            """, delimiter = '|')
    @DisplayName("일 단위 metric으로부터 월간 metric을 생성하면, 월 시작일은 해당 월의 1일로 설정된다")
    void fromDaily_createsMonthlyMetricsWithCorrectMonthStart(LocalDate date, LocalDate monthStart) {

        // given
        ProductMetrics productMetrics = ProductMetrics.builder()
                .productId(1L)
                .likeCount(10L)
                .viewCount(5L)
                .saleQuantity(3L)
                .date(date) // 파라미터 적용
                .build();

        // when
        ProductMetricsMonth productMetricsMonth = ProductMetricsMonth.fromDaily(productMetrics);

        // then
        assertAll(
                () -> assertThat(productMetricsMonth.getProductId()).isEqualTo(1L),
                () -> assertThat(productMetricsMonth.getLikeCount()).isEqualTo(10L),
                () -> assertThat(productMetricsMonth.getViewCount()).isEqualTo(5L),
                () -> assertThat(productMetricsMonth.getSaleQuantity()).isEqualTo(3L),
                () -> assertThat(productMetricsMonth.getMonthStart()).isEqualTo(monthStart)
        );
    }

}
