package com.loopers.domain.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentCommand {
    private Long orderId;
    private String userId;
    private PaymentMethod paymentMethod;
    private Long finalAmount;

    @Builder
    private PaymentCommand(Long orderId, String userId, PaymentMethod paymentMethod, Long finalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.finalAmount = finalAmount;
    }

    public static PaymentCommand of(Long orderId ,String userId , PaymentMethod paymentMethod , Long finalAmount) {
        return PaymentCommand
                .builder()
                .orderId(orderId)
                .userId(userId)
                .paymentMethod(paymentMethod)
                .finalAmount(finalAmount)
                .build();
    }
}
