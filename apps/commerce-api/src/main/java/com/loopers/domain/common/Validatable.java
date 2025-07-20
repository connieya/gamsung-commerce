package com.loopers.domain.common;

import jakarta.validation.*;

import java.util.Set;


public abstract class Validatable<T> {

    private final Validator validator;

    public Validatable() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    protected void validate() {
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
