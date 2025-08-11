package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineJpaRepository extends JpaRepository<OrderLine, Long> {
}
