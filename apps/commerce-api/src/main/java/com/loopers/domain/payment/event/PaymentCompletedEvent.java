package com.loopers.domain.payment.event;

public record PaymentCompletedEvent(
        String orderNumber,
        Long userId,
        Long couponId,
        String status
) {
}
