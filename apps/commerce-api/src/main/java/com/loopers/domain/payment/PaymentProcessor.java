package com.loopers.domain.payment;

public interface PaymentProcessor {

    Payment pay(PaymentCommand paymentCommand);
}
