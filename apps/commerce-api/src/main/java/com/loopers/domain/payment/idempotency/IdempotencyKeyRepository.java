package com.loopers.domain.payment.idempotency;

import java.util.Optional;

public interface IdempotencyKeyRepository {
    IdempotencyKey save(IdempotencyKey idempotencyKey);
    
    Optional<IdempotencyKey> findByOrderNoAndOrderKeyAndOperationType(
            String orderNo, 
            String orderKey, 
            IdempotencyKey.OperationType operationType
    );
}
