package com.loopers.domain.product.sku.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class SkuException {

    public static class SkuNotFoundException extends CoreException {
        public SkuNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class DuplicateOptionCombinationException extends CoreException {
        public DuplicateOptionCombinationException(ErrorType errorType) {
            super(errorType);
        }
    }
}
