package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.PgClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("CARD")
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PgClient pgClient;
    private final PaymentService paymentService;

    @Override
    public Payment pay(PaymentCommand paymentCommand) {
        pgClient.request(paymentCommand.getUserId());

        return paymentService.create(paymentCommand);
    }
}
