package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;


public class PaymentCriteria {
    public record Pay(
            String userId,
            Long orderId,
            PaymentMethod paymentMethod
    ) {
        public PaymentCommand toCommand() {
            return PaymentCommand.of(orderId, userId, paymentMethod);
        }
    }
}
