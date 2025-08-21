package com.loopers.application.payment.processor;

import com.loopers.domain.payment.Payment;

public interface PaymentProcessor {

    Payment pay(PaymentProcessContext command);
}
