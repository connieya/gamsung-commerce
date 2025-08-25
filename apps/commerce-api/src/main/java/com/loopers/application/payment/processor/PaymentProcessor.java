package com.loopers.application.payment.processor;


public interface PaymentProcessor {

    void pay(PaymentProcessContext command);
}
