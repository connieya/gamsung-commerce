package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderCoreRepository implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    @Transactional
    public Order save(Order order) {
        if (order.getId() != null) {
            OrderEntity orderEntity = orderJpaRepository.findById(order.getId())
                    .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));
            orderEntity.complete(order.getOrderStatus());
            orderJpaRepository.save(orderEntity);
            return order;

        }
        return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
    }

    @Override
    public Optional<Order> findOrderDetailById(Long orderId) {
        return orderJpaRepository.findOrderDetailById(orderId) // fetch join 사용 필요
                .map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId).map(OrderEntity::toDomain);
    }
}
