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

    public static OrderLineEntity fromDomain(OrderLine orderLine) {
        OrderLineEntity orderLineEntity = new OrderLineEntity();

        orderLineEntity.productId = orderLine.getProductId();
        orderLineEntity.quantity = orderLine.getQuantity();
        orderLineEntity.orderPrice = orderLine.getPrice();

        return orderLineEntity;
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
