package com.loopers.domain.payment;

public interface PaymentClient {

    PaymentRequestResult request(PaymentCommand.Transaction paymentCommand);

    PaymentTransactionDetail getTransactionDetail(PaymentCommand.Search paymentCommand);
}
