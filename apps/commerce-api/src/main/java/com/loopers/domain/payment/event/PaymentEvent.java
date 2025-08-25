package com.loopers.domain.payment.event;

import com.loopers.domain.order.OrderLine;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.payment.attempt.AttemptStatus;

import java.util.List;

public class PaymentEvent {

    public record Ready(Long paymentId , String orderNumber) {
        public static Ready of(Long paymentId ,String orderNumber) {
            return new Ready(paymentId , orderNumber);
        }
    }

    public record Complete(String transactionKey , Long paymentId , String orderNumber , TransactionStatus status) {
        public static Complete of(String transactionKey , Long paymentId , String orderNumber , TransactionStatus status) {
            return new Complete(transactionKey, paymentId, orderNumber , status);
        }
    }

    public record Failure(Long paymentId , String orderNumber , AttemptStatus status) {
        public static Failure of(Long paymentId , String orderNumber , AttemptStatus status) {
            return new Failure(paymentId , orderNumber , status);
        }
    }

    public record Success(Long orderId , List<OrderLine> orderLines) {
        public static Success of(Long orderId , List<OrderLine> orderLines) {
            return new Success(orderId , orderLines);
        }

    }
}
