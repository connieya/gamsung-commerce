package com.loopers.domain.order.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class OrderException {

    public static class OrderNotFoundException extends CoreException {
        public OrderNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }
}
