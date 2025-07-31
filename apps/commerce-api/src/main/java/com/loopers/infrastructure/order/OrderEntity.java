package com.loopers.infrastructure.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import jakarta.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

@Table(name = "orders")
@Entity
public class OrderEntity extends BaseEntity {

    private Long totalAmount;
    private Long userId;
    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderLineEntity> orderLineEntities;


    public static OrderEntity fromDomain(Order order) {
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.totalAmount = order.getTotalAmount();
        orderEntity.userId = order.getUserId();
        orderEntity.orderLineEntities = order.getOrderLines().stream().map(OrderLineEntity::fromDomain).collect(Collectors.toList());

        return orderEntity;
    }

    public Order toDomain() {
        return Order
                .builder()
                .id(id)
                .userId(userId)
                .totalAmount(totalAmount)
                .orderLines(orderLineEntities.stream().map(OrderLineEntity::toDomain).collect(Collectors.toList()))
                .build();
    }
}
