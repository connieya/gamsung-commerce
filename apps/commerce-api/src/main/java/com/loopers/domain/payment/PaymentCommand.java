package com.loopers.domain.payment;

import lombok.Getter;

@Getter
public class PaymentCommand {
    private Long orderId;
    private String userId;
    private PaymentMethod paymentMethod;
}
