package com.loopers.interfaces.api.internal;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;

import java.util.List;

public class OrderInternalDto {

    public record OrderResponse(
            Long orderId,
            String orderNumber,
            Long totalAmount,
            Long discountAmount,
            Long finalAmount,
            String orderStatus,
            Long userId,
            List<OrderLineResponse> orderLines
    ) {
        public static OrderResponse from(Order order) {
            List<OrderLineResponse> lines = order.getOrderLines().stream()
                    .map(line -> new OrderLineResponse(line.getProductId(), line.getQuantity(), line.getOrderPrice()))
                    .toList();
            return new OrderResponse(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    order.getDiscountAmount(),
                    order.getFinalAmount(),
                    order.getOrderStatus().name(),
                    order.getUserId(),
                    lines
            );
        }
    }

    public record OrderLineResponse(Long productId, Long quantity, Long price) {}
}
