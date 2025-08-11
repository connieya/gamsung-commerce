package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        OrderEntity orderEntity = OrderEntity.fromDomain(order);
        OrderEntity saved = orderJpaRepository.save(orderEntity); // cascade로 하위 저장됨
        return saved.toDomain();
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
