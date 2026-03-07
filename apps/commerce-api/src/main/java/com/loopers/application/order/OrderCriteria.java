package com.loopers.application.order;

import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentMethod;

import java.util.List;

public class OrderCriteria {

    public record OrderItem(Long productId, Long quantity) {}

    public record Ready(
            PaymentMethod paymentMethod,
            PayKind payKind,
            String userId,
            List<OrderItem> orderItems,
            Long couponId
    ) {}
}
