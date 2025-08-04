package com.loopers.domain.user.exception;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class UserException {

    public static class UserAlreadyExistsException extends CoreException {

        public UserAlreadyExistsException(ErrorType errorType) {
            super(errorType);
        }
    }

    public static class UserNotFoundException extends CoreException {
        public UserNotFoundException(ErrorType errorType) {
            super(errorType);
        }
    }
}
