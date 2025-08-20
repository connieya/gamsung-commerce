package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.PaymentAdapter;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PgSimulator implements PaymentAdapter {

    private final PgSimulatorClient client;


    @Override
    public void request(PaymentCommand paymentCommand) {
        PgSimulatorRequest.RequestTransaction requestTransaction = PgSimulatorRequest.RequestTransaction.of(String.valueOf(paymentCommand.getOrderId()), "1234-1234-1234", paymentCommand.getFinalAmount(), "", PgSimulatorRequest.CardType.KB);
        ApiResponse<PgSimulatorResponse.RequestTransaction> response = client.request(paymentCommand.getUserId(), requestTransaction);
        PgSimulatorResponse.RequestTransaction data = response.data();
        System.out.println("data.reason() = " + data.reason());
        System.out.println("data.transactionKey() = " + data.transactionKey());

    }
}
