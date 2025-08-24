package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentResult;

public interface PaymentProcessor {

    PaymentResult pay(PaymentProcessContext command);
}
