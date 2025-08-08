package com.loopers.domain.order;

import lombok.*;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCommand {
    private Long userId;
    private List<OrderItem> orderItems;
    private Long discountAmount;
    private String idempotencyKey;


    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderItem {
        private final Long productId;
        private final Long quantity;
        private final Long price;
    }

    @Builder
    private OrderCommand(Long userId,  List<OrderItem> orderItems , Long discountAmount , String idempotencyKey) {
        this.userId = userId;
        this.orderItems = orderItems;
        this.discountAmount = discountAmount;
        this.idempotencyKey = idempotencyKey;

    }

    public static OrderCommand of(Long userId, List<OrderItem> orderItems, Long discountAmount , String idempotencyKey) {
        return OrderCommand
                .builder()
                .userId(userId)
                .orderItems(orderItems)
                .discountAmount(discountAmount)
                .build();
    }
}
