package com.loopers.infrastructure.payment.client;

public class PgSimulatorResponse {

    public record RequestTransaction(
            String transactionKey,
            String status,
            String reason
    ) {
    }
}
