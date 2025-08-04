package com.loopers.domain.brand.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class BrandException {

    public static class BrandNotFoundException extends CoreException {
        public BrandNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }
}
