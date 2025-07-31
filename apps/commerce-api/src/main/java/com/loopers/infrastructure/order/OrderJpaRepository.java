package com.loopers.infrastructure.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    @Query("select o from OrderEntity o join fetch o.orderLineEntities ol where  o.id = :orderId")
    Optional<OrderEntity> findOrderDetailById(@Param("orderId") Long orderId);
}
