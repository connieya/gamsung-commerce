package com.loopers.infrastructure.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Table(name = "orders")
@Entity
@Getter
public class OrderEntity extends BaseEntity {

    private Long totalAmount;

    @Column(name = "ref_user_id" , nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineEntity> orderLineEntities = new ArrayList<>();

    private Long discountAmount;

    @Column(name = "order_status" , nullable = false)
    private OrderStatus orderStatus;



    public static OrderEntity fromDomain(Order order) {
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.totalAmount = order.getTotalAmount();
        orderEntity.userId = order.getUserId();
        orderEntity.discountAmount = order.getDiscountAmount();

        // 연관관계 주입
        order.getOrderLines().forEach(orderLine -> {
            OrderLineEntity lineEntity = OrderLineEntity.fromDomain(orderLine, orderEntity);
            orderEntity.orderLineEntities.add(lineEntity);
        });

        return orderEntity;
    }


    public Order toDomain() {
        return Order
                .builder()
                .id(id)
                .userId(userId)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .orderLines(orderLineEntities.stream().map(OrderLineEntity::toDomain).collect(Collectors.toList()))
                .build();
    }

}
