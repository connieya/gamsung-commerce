package com.loopers.domain.order;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Order {

    private Long id;
    private String orderNumber;
    private Long totalAmount;
    private Long userId;
    private List<OrderLine> orderLines;



    @Builder
    private Order(Long id, String orderNumber, Long totalAmount, Long userId, List<OrderLine> orderLines) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.orderLines = orderLines;
    }

    public static Order create(OrderCommand orderCommand) {
        return Order
                .builder()
                .totalAmount(orderCommand.getTotalAmount())
                .build();
    }
}
