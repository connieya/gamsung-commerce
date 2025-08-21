package com.loopers.domain.payment;

public interface PaymentAdapter {

    void request(PaymentCommand.Transaction paymentCommand);

    void getTransactionDetail(PaymentCommand.Search paymentCommand);

}
