package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;


public class PaymentCriteria {
    public record Pay(
            String userId,
            Long orderId,
            PaymentMethod paymentMethod,
            CardType cardType,
            String cardNumber,
            Long couponId
    ) {
        public static Pay of(String userId, Long orderId, PaymentMethod paymentMethod, CardType cardType, String cardNumber ,Long couponId) {
            return new Pay(userId, orderId, paymentMethod, cardType, cardNumber , couponId);
        }
    }

    public record Complete(
            String transactionKey,
            String orderNumber,
            CardType cardType,
            String cardNo,
            Long amount
    ) {
        public static Complete of(String transactionKey, String orderNumber, CardType cardType, String cardNo, Long amount) {
            return new Complete(transactionKey, orderNumber, cardType, cardNo, amount);
        }
    }
}
