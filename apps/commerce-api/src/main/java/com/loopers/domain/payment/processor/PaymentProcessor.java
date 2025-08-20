package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;

public interface PaymentProcessor {

    Payment pay(PaymentCommand paymentCommand);
}
