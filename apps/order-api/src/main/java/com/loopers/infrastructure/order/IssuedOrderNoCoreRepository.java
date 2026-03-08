package com.loopers.infrastructure.order;

import com.loopers.domain.order.IssuedOrderNo;
import com.loopers.domain.order.IssuedOrderNoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class IssuedOrderNoCoreRepository implements IssuedOrderNoRepository {

    private final IssuedOrderNoJpaRepository jpaRepository;

    @Override
    public IssuedOrderNo save(IssuedOrderNo issuedOrderNo) {
        return jpaRepository.save(issuedOrderNo);
    }

    @Override
    public Optional<IssuedOrderNo> findByOrderNo(String orderNo) {
        return jpaRepository.findByOrderNo(orderNo);
    }
}
