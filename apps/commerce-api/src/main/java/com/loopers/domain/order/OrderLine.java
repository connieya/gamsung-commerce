package com.loopers.domain.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "order_line")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderLine extends BaseEntity {

    private Long productId;
    private Long quantity;
    private Long orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    private OrderLine(Long productId, Long quantity, Long orderPrice, Order order) {
        this.productId = productId;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.order = order;
    }

    public static OrderLine fromDomain(OrderLine orderLine, Order parentOrder) {
        OrderLine entity = new OrderLine();
        entity.productId = orderLine.getProductId();
        entity.quantity = orderLine.getQuantity();
        entity.orderPrice = orderLine.getOrderPrice();
        entity.order = parentOrder;
        return entity;
    }

    public static OrderLine create(Long productId, Long quantity, Long price) {
        return OrderLine
                .builder()
                .productId(productId)
                .quantity(quantity)
                .orderPrice(price)
                .build();
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
