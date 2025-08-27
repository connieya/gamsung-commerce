package com.loopers.domain.payment.event;

import com.loopers.domain.order.OrderLine;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.payment.attempt.AttemptStatus;

import java.util.List;

public class PaymentEvent {

    public record Ready(Long orderId, String orderNumber, Long userId, Long totalAmount,
                        PaymentMethod paymentMethod) {
        public static Ready of(Long orderId, String orderNumber, Long userId, Long totalAmount, PaymentMethod paymentMethod) {
            return new Ready(orderId, orderNumber, userId, totalAmount, paymentMethod);
        }
    }

    public record Complete(String transactionKey, Long paymentId, String orderNumber, TransactionStatus status) {
        public static Complete of(String transactionKey, Long paymentId, String orderNumber, TransactionStatus status) {
            return new Complete(transactionKey, paymentId, orderNumber, status);
        }
    }

    public record Failure(Long paymentId, String orderNumber, AttemptStatus status) {
        public static Failure of(Long paymentId, String orderNumber, AttemptStatus status) {
            return new Failure(paymentId, orderNumber, status);
        }
    }

    public record Success(Long paymentId, Long orderId, String orderNumber, String userId, PaymentMethod paymentMethod,
                          Long finalAmount,
                          List<OrderLine> orderLines) {
        public static Success of(Long paymentId, Long orderId, String orderNumber, String userId, PaymentMethod paymentMethod, Long finalAmount, List<OrderLine> orderLines) {
            return new Success(paymentId, orderId, orderNumber, userId, paymentMethod, finalAmount, orderLines);
        }

    }
}
