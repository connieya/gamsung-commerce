package com.loopers.domain.order;

import lombok.*;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCommand {
    private Long userId;
    private Long totalAmount;
    private List<OrderItem> orderItems;

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderItem {
        private final Long productId;
        private final Long quantity;
        private final Long price;
    }

    @Builder
    private OrderCommand(Long userId, Long totalAmount, List<OrderItem> orderItems) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
    }

    public static OrderCommand of(Long userId, List<OrderItem> orderItems, Long totalAmount) {
        return OrderCommand
                .builder()
                .userId(userId)
                .orderItems(orderItems)
                .totalAmount(totalAmount)
                .build();
    }
}
