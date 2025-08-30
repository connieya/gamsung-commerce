package com.loopers.domain.payment.exception;

import com.loopers.domain.payment.attempt.AttemptStatus;

public interface PaymentFailure {
    AttemptStatus attemptStatus();
}
