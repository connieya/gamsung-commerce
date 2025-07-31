package com.loopers.domain.point.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PointException {
    public static class PointInvalidChargeAmountException extends CoreException {
        public PointInvalidChargeAmountException(ErrorType errorType) {
            super(errorType);
        }
    }
}
