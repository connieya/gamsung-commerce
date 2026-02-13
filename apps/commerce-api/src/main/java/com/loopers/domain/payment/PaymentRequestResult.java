package com.loopers.domain.payment;

public record PaymentRequestResult(
        String transactionKey,
        TransactionStatus status,
        String reason
) {
}
