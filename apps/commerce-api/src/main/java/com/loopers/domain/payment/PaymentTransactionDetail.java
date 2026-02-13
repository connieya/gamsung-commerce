package com.loopers.domain.payment;

public record PaymentTransactionDetail(
        String transactionKey,
        String orderNumber,
        CardType cardType,
        String cardNumber,
        Long amount,
        TransactionStatus transactionStatus
) {
}
