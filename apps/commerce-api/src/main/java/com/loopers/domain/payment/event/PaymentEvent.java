package com.loopers.domain.payment.event;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.payment.attempt.AttemptStatus;

import java.util.List;

public class PaymentEvent {

    public record Complete(String transactionKey, String orderNumber, TransactionStatus status, Long couponId, Long userId) {
        public static Complete of(String transactionKey, String orderNumber, TransactionStatus status, Long couponId, Long userId) {
            return new Complete(transactionKey, orderNumber, status, couponId, userId);
        }
    }

    public record Failure(String orderNumber, AttemptStatus status) {
        public static Failure of(String orderNumber, AttemptStatus status) {
            return new Failure(orderNumber, status);
        }
    }

    public record OrderLineSnapshot(Long productId, Long quantity, Long price) {}

    public record Success(Long orderId, String orderNumber, Long userId, PaymentMethod paymentMethod,
                          Long finalAmount,
                          List<OrderLineSnapshot> orderLines, Long couponId) {
        public static Success of(Long orderId, String orderNumber, Long userId, PaymentMethod paymentMethod, Long finalAmount, List<OrderLineSnapshot> orderLines, Long couponId) {
            return new Success(orderId, orderNumber, userId, paymentMethod, finalAmount, orderLines, couponId);
        }
    }
}
