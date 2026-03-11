package com.loopers.domain.coupon.event;

public record PaymentCompletedEvent(
        String orderNumber,
        Long userId,
        Long couponId,
        String status
) {
}
