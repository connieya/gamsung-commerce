package com.loopers.domain.payment.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentException {
    public static class PaymentNotFoundException extends CoreException {

        public PaymentNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class PaymentRequestFailedException extends CoreException {
        public PaymentRequestFailedException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class PgTimeoutException extends CoreException {
        public PgTimeoutException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class CircuitOpenException extends CoreException {
        public CircuitOpenException(ErrorType errorType) {
            super(errorType);
        }
    }
}
