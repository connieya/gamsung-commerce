package com.loopers.application.product.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class ProductException {

    public static class ProductNotFoundException extends CoreException {
        public ProductNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }
}
