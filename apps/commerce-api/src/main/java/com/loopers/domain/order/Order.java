package com.loopers.domain.order;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Order {

    private Long id;
    private Long totalAmount;
    private Long userId;
    private List<OrderLine> orderLines;
    private Long discountAmount;


    @Builder
    private Order(Long id, Long totalAmount, Long userId, List<OrderLine> orderLines) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.orderLines = orderLines;
    }

    public static Order create(OrderCommand orderCommand) {
        List<OrderLine> convert = orderCommand.getOrderItems()
                .stream()
                .map(item ->
                        OrderLine.create(item.getProductId(), item.getQuantity(), item.getPrice())
                ).toList();
        return Order
                .builder()
                .userId(orderCommand.getUserId())
                .totalAmount(orderCommand.getTotalAmount())
                .orderLines(convert)
                .build();
    }
}
