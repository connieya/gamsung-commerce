package com.loopers.domain.payment;

import lombok.Getter;

@Getter
public class PaymentCommand {

    public record Create(
            Long orderId,
            String userId,
            PaymentMethod paymentMethod,
            Long finalAmount
    ) {
        public static Create of(Long orderId, String userId, PaymentMethod paymentMethod, Long finalAmount) {
            return new Create(orderId, userId, paymentMethod, finalAmount);
        }
    }

    public record Transaction(String orderId, CardType cardType, String cardNumber, Long amount) {
        public static Transaction of(String orderId, CardType cardType, String cardNumber, Long amount) {
            return new Transaction(orderId, cardType, cardNumber, amount);
        }

    }

}
