package com.loopers.domain.order;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class OrderInfo {

    private Long orderId;
    private String orderNumber;
    private Long totalAmount;
    private List<OrderItem> orderItems;
    private Long discountAmount;
    private OrderStatus orderStatus;
    private ZonedDateTime createdAt;

    @Builder
    private OrderInfo(Long orderId, String orderNumber, Long totalAmount, List<OrderItem> orderItems, 
                     Long discountAmount, OrderStatus orderStatus, ZonedDateTime createdAt) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
        this.discountAmount = discountAmount;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
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
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .orderItems(order.getOrderLines().stream().map(orderLine -> OrderItem.
                                builder()
                                .productId(orderLine.getProductId())
                                .quantity(orderLine.getQuantity())
                                .price(orderLine.getOrderPrice())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();
    }

}
