package com.loopers.infrastructure.feign.order;

import java.util.List;

public class OrderApiDto {

    public record OrderResponse(
            Long orderId,
            String orderNumber,
            Long totalAmount,
            Long discountAmount,
            Long finalAmount,
            String orderStatus,
            Long userId,
            List<OrderLineResponse> orderLines
    ) {}

    public record OrderLineResponse(
            Long productId,
            Long quantity,
            Long price
    ) {}
}
