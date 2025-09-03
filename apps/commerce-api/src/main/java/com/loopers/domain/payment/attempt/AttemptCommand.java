package com.loopers.domain.payment.attempt;

import com.loopers.domain.payment.TransactionStatus;

public class AttemptCommand {

    public record Request(Long paymentId, String orderNumber) {
        public static Request of(Long paymentId, String orderNumber) {
            return new Request(paymentId, orderNumber);
        }
    }

    public record Complete(String transactionKey, Long paymentId, String orderNumber, TransactionStatus transactionStatus) {
        public static Complete of(String transactionKey, Long paymentId, String orderNumber, TransactionStatus transactionStatus) {
            return new Complete(transactionKey, paymentId, orderNumber, transactionStatus);
        }
    }

    public record Failure(Long paymentId, String orderNumber, AttemptStatus attemptStatus) {
        public static Failure of(Long paymentId, String orderNumber, AttemptStatus attemptStatus) {
            return new Failure(paymentId, orderNumber, attemptStatus);
        }
    }

    public record Success(Long paymentId, String orderNumber, AttemptStatus attemptStatus) {
        public static Success of(Long paymentId, String orderNumber, AttemptStatus attemptStatus) {
            return new Success(paymentId, orderNumber, attemptStatus);
        }
    }
}
