package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("CARD")
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentAdapter paymentAdapter;
    private final PaymentService paymentService;

    @Override
    public Payment pay(PaymentCommand paymentCommand) {
        Payment payment = paymentService.create(paymentCommand, PaymentStatus.PENDING);
        paymentAdapter.request(paymentCommand);

        return payment;
    }
}
