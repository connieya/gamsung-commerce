package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.PgClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAdapter {
    private final PgClient pgClient;

    public void request() {


    }
}
