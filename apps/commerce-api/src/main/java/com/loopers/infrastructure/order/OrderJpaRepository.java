package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderLines WHERE o.id = :id")
    Optional<Order> findOrderDetailById(@Param("id") Long id);

    @Query("update Order o set o.orderStatus  = :orderStatus where o.id = :id")
    @Modifying
    void complete(@Param("orderStatus") OrderStatus orderStatus , @Param("id") Long id);
}
