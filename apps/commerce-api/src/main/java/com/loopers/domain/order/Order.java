package com.loopers.domain.order;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Order {

    private Long id;
    private String orderNumber;
    private Long totalAmount;
    private OrderStatus orderStatus;
    private Long userId;
    private List<OrderLine> orderLines;


    public enum OrderStatus {
        PENDING,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED
    }

    @Builder
    private Order(Long id, String orderNumber, Long totalAmount, OrderStatus orderStatus, Long userId, List<OrderLine> orderLines) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.userId = userId;
        this.orderLines = orderLines;
    }

    public static Order create(Long userId) {

        return null;
    }
}
