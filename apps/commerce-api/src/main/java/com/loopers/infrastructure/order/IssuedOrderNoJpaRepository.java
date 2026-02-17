package com.loopers.infrastructure.order;

import com.loopers.domain.order.IssuedOrderNo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssuedOrderNoJpaRepository extends JpaRepository<IssuedOrderNo, Long> {
    Optional<IssuedOrderNo> findByOrderNo(String orderNo);
}
