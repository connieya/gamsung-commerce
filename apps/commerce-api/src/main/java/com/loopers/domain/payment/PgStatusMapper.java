package com.loopers.domain.payment;

import com.loopers.domain.payment.attempt.AttemptStatus;

public class PgStatusMapper {
    public static AttemptStatus toAttemptStatus(TransactionStatus ts) {
        return switch (ts) {
            case SUCCESS -> AttemptStatus.SUCCESS;
            case FAILED  -> AttemptStatus.FAILED;
            case PENDING -> AttemptStatus.REQUESTED;
        };
    }
}
