package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.Payment;
import lombok.Getter;

@Getter
public class PaymentResult {
    private Long paymentId;
    private PaymentStatus paymentStatus;

    public PaymentResult(Long paymentId, PaymentStatus paymentStatus) {
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
    }

    public static PaymentResult from(Payment payment) {
        return new PaymentResult(payment.getId(),payment.getPaymentStatus());
    }
}
