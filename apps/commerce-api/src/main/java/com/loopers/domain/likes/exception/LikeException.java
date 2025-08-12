package com.loopers.domain.likes.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class LikeException {
    public static class LikeSummaryNotFoundException extends CoreException {
        public LikeSummaryNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class LikeCountCannotBeNegativeException extends CoreException {
        public LikeCountCannotBeNegativeException(ErrorType errorType) {
            super(errorType);
        }
    }

}
