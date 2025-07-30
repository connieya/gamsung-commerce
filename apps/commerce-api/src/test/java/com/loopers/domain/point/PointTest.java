package com.loopers.domain.point;

import com.loopers.domain.point.exception.PointException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PointTest {

    @DisplayName("0 미만의 정수로 포인트 생성 시 실패한다.")
    @ParameterizedTest
    @ValueSource(longs = {
            -1L,
            -100L,
            -1000L,
    })
    void chargeFail_whenValueIsZeroOrLess(Long value) {
        // given
        String userId = "geonhee";

        // when & then
        assertThatThrownBy(() -> {
            Point.create(userId, value);
        }).isInstanceOf(ConstraintViolationException.class);
    }


    @DisplayName("0 이하의 정수로 포인트 충전 시 실패한다.")
    @ParameterizedTest
    @ValueSource(longs = {
            0L,
            -1L,
    })
    void chargeFail_whenChargeValueIsZeroOrLess(Long value) {
        // given
        String userId = "geonhee";

        Point point = Point.create(userId, 0L);

        // when & then
        assertThatThrownBy(() -> {
            point.charge(value);
        }).isInstanceOf(PointException.PointInvalidChargeAmountException.class);
    }

}
