package com.loopers.infrastructure.payment.idempotency;

import com.loopers.domain.payment.idempotency.IdempotencyKey;
import com.loopers.domain.payment.idempotency.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdempotencyKeyCoreRepository implements IdempotencyKeyRepository {
    
    private final IdempotencyKeyJpaRepository jpaRepository;
    
    @Override
    public IdempotencyKey save(IdempotencyKey idempotencyKey) {
        return jpaRepository.save(idempotencyKey);
    }
    
    @Override
    public Optional<IdempotencyKey> findByOrderNoAndOrderKeyAndOperationType(
            String orderNo, 
            String orderKey, 
            IdempotencyKey.OperationType operationType
    ) {
        return jpaRepository.findByOrderNoAndOrderKeyAndOperationType(orderNo, orderKey, operationType);
    }
}
