package com.loopers.domain.payment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Payment {
    private Long id;
    private Long amount;
    private Long orderId;
    private Long userId;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;


    @Builder
    private Payment(Long id, Long amount, Long orderId, Long userId, PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
        this.id = id;
        this.amount = amount;
        this.orderId = orderId;
        this.userId = userId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }

    public static Payment create(Long amount , Long orderId , Long userId , PaymentMethod paymentMethod , PaymentStatus paymentStatus){
        return Payment
                .builder()
                .amount(amount)
                .orderId(orderId)
                .userId(userId)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();

    }
}
