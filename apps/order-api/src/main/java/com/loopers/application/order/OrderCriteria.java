package com.loopers.application.order;

import java.util.List;

public class OrderCriteria {

    public record OrderItem(Long productId, Long quantity) {}

    public record Ready(
            String paymentMethod,
            String payKind,
            String userId,
            List<OrderItem> orderItems,
            Long couponId
    ) {}
}
