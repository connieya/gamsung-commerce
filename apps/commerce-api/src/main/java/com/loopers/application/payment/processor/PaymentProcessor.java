package com.loopers.application.payment.processor;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;

public interface PaymentProcessor {

    Payment pay(PaymentProcessContext command);
}
