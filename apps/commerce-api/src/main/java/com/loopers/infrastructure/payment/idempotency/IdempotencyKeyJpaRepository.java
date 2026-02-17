package com.loopers.infrastructure.payment.idempotency;

import com.loopers.domain.payment.idempotency.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByOrderNoAndOrderKeyAndOperationType(
            String orderNo,
            String orderKey,
            IdempotencyKey.OperationType operationType
    );
}
