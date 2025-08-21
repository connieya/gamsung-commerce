package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.CardType;

public class PgSimulatorRequest {

    public record RequestTransaction(
            String orderId,
            String cardNo,
            Long amount,
            String callbackUrl,
            CardType cardType
    ) {
        public static RequestTransaction of(String orderId, String cardNo, Long amount, String callbackUrl, CardType cardType) {
            return new RequestTransaction(orderId, cardNo, amount, callbackUrl, cardType);
        }
    }
}
