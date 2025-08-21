package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.client.PgSimulatorResponse;

public interface PaymentAdapter {

    void request(PaymentCommand.Transaction paymentCommand);

    PgSimulatorResponse.TransactionDetail getTransactionDetail(PaymentCommand.Search paymentCommand);

}
