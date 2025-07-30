package com.loopers.domain.product.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class BrandException {

    public static class BrandNotFoundException extends CoreException {
        public BrandNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }
}
