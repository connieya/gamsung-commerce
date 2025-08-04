package com.loopers.domain.order;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class OrderInfo {

    private Long totalAmount;
    private List<OrderItem> orderItems;

    @Builder
    private OrderInfo(Long totalAmount, List<OrderItem> orderItems) {
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
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
                .totalAmount(order.getTotalAmount())
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
