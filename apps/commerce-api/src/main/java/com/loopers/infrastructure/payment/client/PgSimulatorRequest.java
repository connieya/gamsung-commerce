package com.loopers.infrastructure.payment.client;

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


    public enum CardType {
        SAMSUNG, KB , HYUNDAI , SHINHAN
    }
}
