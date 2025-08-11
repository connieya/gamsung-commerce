package com.loopers.domain.coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum CouponType {

    // 정액 할인 (고정 금액)
    FIXED_AMOUNT {
        @Override
        public Long calculate(Long orderAmount, Long discountValue) {
            // 주문 금액이 할인 금액보다 작으면 0원 할인
            if (orderAmount < discountValue) {
                return orderAmount;
            }
            return discountValue;
        }
    },

    // 정률 할인 (비율)
    PERCENTAGE {
        @Override
        public Long calculate(Long orderAmount, Long discountValue) {
            BigDecimal orderAmountBigDecimal = new BigDecimal(orderAmount);
            BigDecimal discountValueBigDecimal = new BigDecimal(discountValue);

            // 할인율 계산 (예: 15% -> 0.15)
            BigDecimal discountRate = discountValueBigDecimal.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            // 할인 금액 = 주문 금액 * 할인율
            BigDecimal discountAmount = orderAmountBigDecimal.multiply(discountRate);

            // 소수점 반올림 후 long 타입으로 변환
            return discountAmount.setScale(0, RoundingMode.HALF_UP).longValue();
        }
    };

    // 각 상수가 구현해야 할 추상 메서드
    public abstract Long calculate(Long orderAmount, Long discountValue);
}
