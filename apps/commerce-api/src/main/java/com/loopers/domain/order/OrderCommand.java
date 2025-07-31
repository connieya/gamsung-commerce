package com.loopers.domain.order;

import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCommand {
    private String userId;
    private Long totalAmount;

    @Builder
    private OrderCommand(String userId, Long totalAmount) {
        this.userId = userId;

        this.totalAmount = totalAmount;
    }

    public static OrderCommand of(String userId, Long totalAmount) {
        return OrderCommand
                .builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .build();
    }
}
