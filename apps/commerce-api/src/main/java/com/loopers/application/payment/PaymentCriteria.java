package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.interfaces.api.order.OrderV1Dto;

import java.util.List;


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
    
    public record Ready(
            PaymentMethod paymentMethod,
            String userId,
            List<com.loopers.interfaces.api.order.OrderV1Dto.OrderItem> orderItems,
            Long couponId
    ) {}
    
    public record PaymentSession(
            PaymentMethod paymentMethod,
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
