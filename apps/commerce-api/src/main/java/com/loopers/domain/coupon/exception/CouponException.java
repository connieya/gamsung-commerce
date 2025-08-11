package com.loopers.domain.coupon.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class CouponException {

    public static class CouponNotFoundException extends CoreException {
        public CouponNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class UserCouponNotFoundException extends CoreException {
        public UserCouponNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class UserCouponAlreadyUsedException extends CoreException {
        public UserCouponAlreadyUsedException(ErrorType errorType) {
            super(errorType);
        }
    }
}
