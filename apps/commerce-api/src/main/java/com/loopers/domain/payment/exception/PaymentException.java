package com.loopers.domain.payment.exception;

import com.loopers.domain.payment.attempt.AttemptStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentException {
    public static class PaymentNotFoundException extends CoreException {

        public PaymentNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class PaymentRequestFailedException extends CoreException implements PaymentFailure {
        public PaymentRequestFailedException(ErrorType errorType) {
            super(errorType);
        }

        @Override
        public AttemptStatus attemptStatus() {
            return AttemptStatus.FAILED;
        }
    }

    public static class PgTimeoutException extends CoreException implements PaymentFailure {
        public PgTimeoutException(ErrorType errorType) {
            super(errorType);
        }

        @Override
        public AttemptStatus attemptStatus() {
            return AttemptStatus.TIMEOUT;
        }
    }

    public static class CircuitOpenException extends CoreException implements PaymentFailure {
        public CircuitOpenException(ErrorType errorType) {
            super(errorType);
        }

        @Override
        public AttemptStatus attemptStatus() {
            return AttemptStatus.PROVIDER_UNAVAILABLE;
        }
    }
}
