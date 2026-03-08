package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;

import java.util.List;

public class PaymentV1Dto {

    public static class Request {
        public record Pay(
                Long orderId,
                PaymentMethod paymentMethod,
                PayKind payKind,
                CardType cardType,
                String cardNumber,
                Long couponId
        ) {}

        public record PaymentSession(
                String orderNo,
                String orderKey,
                PaymentMethod paymentMethod,
                PayKind payKind,
                List<OrderItem> orderItems,
                CardType cardType,
                String cardNumber,
                Long couponId
        ) {}

        public record OrderItem(Long productId, Long quantity) {}

        public record CallbackTransaction(
            String transactionKey,
            String orderId,
            CardType cardType,
            String cardNo,
            Long amount
        ) {}
    }

    public static class Response {
        public record Pay(
                Long paymentId,
                PaymentStatus paymentStatus) {
            public static Pay from(PaymentResult paymentResult) {
                return new Pay(paymentResult.getPaymentId(), paymentResult.getPaymentStatus());
            }
        }

        public record PaymentSession(
                String orderNo,
                String paymentKey,
                Long amount,
                String paymentUrl,
                String pgKind
        ) {
            public static PaymentSession from(PaymentInfo.SessionResult result) {
                return new PaymentSession(
                        result.orderNo(),
                        result.paymentKey(),
                        result.amount(),
                        result.paymentUrl(),
                        result.pgKind()
                );
            }
        }
    }
}
