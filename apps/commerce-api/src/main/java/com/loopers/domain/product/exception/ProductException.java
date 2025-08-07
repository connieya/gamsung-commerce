package com.loopers.domain.product.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class ProductException {

    public static class ProductNotFoundException extends CoreException {
        public ProductNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class InsufficientStockException extends CoreException {
        public InsufficientStockException(ErrorType errorType) {
            super(errorType);
        }
    }
}
