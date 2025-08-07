package com.loopers.domain.order;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class OrderInfo {

    private Long orderId;
    private Long totalAmount;
    private List<OrderItem> orderItems;
    private Long discountAmount;

    @Builder
    private OrderInfo(Long orderId, Long totalAmount, List<OrderItem> orderItems, Long discountAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
        this.discountAmount = discountAmount;
    }


    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderItem {
        private final Long productId;
        private final Long quantity;
        private final Long price;
    }

    public static OrderInfo from(Order order) {
        return OrderInfo
                .builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .orderItems(order.getOrderLines().stream().map(orderLine -> OrderItem.
                                builder()
                                .productId(orderLine.getProductId())
                                .quantity(orderLine.getQuantity())
                                .price(orderLine.getPrice())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();
    }

}
