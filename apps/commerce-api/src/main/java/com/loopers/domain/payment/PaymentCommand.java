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

    public record Execute(
            String transactionKey,
            TransactionStatus transactionStatus,
            Long paymentId
    ) {
        public static Execute of(String transactionKey, TransactionStatus transactionStatus, Long paymentId) {
            return new Execute(transactionKey, transactionStatus, paymentId);
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
