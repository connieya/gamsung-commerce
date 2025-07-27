package com.loopers.domain.order;

import java.util.List;

public class Order {

    private String orderNumber;
    private Long totalAmount;
    private OrderStatus orderStatus;
    private User user;
    private List<OrderItem> orderItems;


    public enum OrderStatus {
        PENDING,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED
    }
}
