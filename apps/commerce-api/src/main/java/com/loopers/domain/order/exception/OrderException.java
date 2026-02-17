package com.loopers.domain.order.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class OrderException {

    public static class OrderNotFoundException extends CoreException {
        public OrderNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class OrderInvalidStatusException extends CoreException {
        public OrderInvalidStatusException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class OrderInvalidAmountException extends CoreException {
        public OrderInvalidAmountException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class OrderNoNotIssuedException extends CoreException {
        public OrderNoNotIssuedException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class OrderSignatureInvalidException extends CoreException {
        public OrderSignatureInvalidException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class OrderNoAlreadyUsedException extends CoreException {
        public OrderNoAlreadyUsedException(ErrorType errorType) {
            super(errorType);
        }
    }
}
