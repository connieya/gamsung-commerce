package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.TransactionStatus;

public class PgSimulatorResponse {

    public record RequestTransaction(
            String transactionKey,
            TransactionStatus status,
            String reason
    ) {
    }

    public record TransactionDetail(
            String transactionKey,
            String orderNumber,
            CardType cardType,
            String cardNumber,
            Long amount,
            TransactionStatus transactionStatus
    ) {

    }
}
