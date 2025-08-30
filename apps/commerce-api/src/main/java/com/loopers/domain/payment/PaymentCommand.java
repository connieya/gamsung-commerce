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

    public record Transaction(Long orderId, String orderNumber, CardType cardType, String cardNumber,
                              Long amount, Long userId, Long couponId) {
        public static Transaction of(Long orderId, String orderNumber, CardType cardType, String cardNumber, Long amount, Long userId, Long couponId) {
            return new Transaction(orderId, orderNumber, cardType, cardNumber, amount, userId, couponId);
        }
    }

    public record Ready(Long orderId, String orderNumber, Long userId, Long totalAmount,
                        PaymentMethod paymentMethod) {
        public static PaymentCommand.Ready of(Long orderId, String orderNumber, Long userId, Long totalAmount, PaymentMethod paymentMethod) {
            return new PaymentCommand.Ready(orderId, orderNumber, userId, totalAmount, paymentMethod);
        }
    }

    public record Search(
            String transactionKey,
            String orderNumber
    ) {

        public static Search of(String transactionKey, String orderNumber) {
            return new Search(transactionKey, orderNumber);
        }
    }

}
