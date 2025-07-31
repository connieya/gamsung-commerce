package com.loopers.domain.point;

import com.loopers.domain.point.exception.PointException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("주문한 금액만큼 포인트를 차감한다.")
    void deduct() {
        // given
        Point point = Point.create("gunny", 10000L);

        // when
        point.deduct(3000L);

        // then
        assertThat(point.getValue()).isEqualTo(7000L);
    }


    @Test
    @DisplayName("보유한 포인트보다 주문 금액이 많은 경우 PointInsufficientException 예외가 발생한다.")
    void deduct_Fail() {
        // given
        Point point = Point.create("gunny", 10000L);

        // when & then
        assertThatThrownBy(() -> {
            point.deduct(13000L);
        }).isInstanceOf(PointException.PointInsufficientException.class);
    }

}
