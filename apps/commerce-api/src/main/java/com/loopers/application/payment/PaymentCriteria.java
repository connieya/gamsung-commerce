package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentMethod;

import java.util.List;


public class PaymentCriteria {

    public record OrderItem(Long productId, Long quantity) {}

    public record Pay(
            String userId,
            Long orderId,
            PaymentMethod paymentMethod,
            PayKind payKind,
            CardType cardType,
            String cardNumber,
            Long couponId
    ) {
        public static Pay of(
                String userId,
                Long orderId,
                PaymentMethod paymentMethod,
                PayKind payKind,
                CardType cardType,
                String cardNumber,
                Long couponId
        ) {
            return new Pay(userId, orderId, paymentMethod, payKind, cardType, cardNumber, couponId);
        }
    }

    public record PaymentSession(
            PaymentMethod paymentMethod,
            PayKind payKind,
            String userId,
            List<OrderItem> orderItems,
            CardType cardType,
            String cardNumber,
            Long couponId
    ) {}

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
