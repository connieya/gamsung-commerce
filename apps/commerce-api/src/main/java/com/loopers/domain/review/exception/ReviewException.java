package com.loopers.domain.review.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class ReviewException {

    public static class ReviewNotFoundException extends CoreException {
        public ReviewNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class ReviewAlreadyExistsException extends CoreException {
        public ReviewAlreadyExistsException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class ReviewOrderNotCompletedException extends CoreException {
        public ReviewOrderNotCompletedException(ErrorType errorType) {
            super(errorType);
        }
    }
}
