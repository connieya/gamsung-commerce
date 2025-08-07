package com.loopers.infrastructure.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.OrderLine;
import jakarta.persistence.*;

@Table(name = "order_line")
@Entity
public class OrderLineEntity extends BaseEntity {

    private Long productId;
    private Long quantity;
    private Long orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    public static OrderLineEntity fromDomain(OrderLine orderLine, OrderEntity parentOrder) {
        OrderLineEntity entity = new OrderLineEntity();
        entity.productId = orderLine.getProductId();
        entity.quantity = orderLine.getQuantity();
        entity.orderPrice = orderLine.getPrice();
        entity.order = parentOrder;
        return entity;
    }

    public OrderLine toDomain() {
        return OrderLine
                .builder()
                .id(id)
                .price(orderPrice)
                .productId(productId)
                .quantity(quantity)
                .build();
    }


}
