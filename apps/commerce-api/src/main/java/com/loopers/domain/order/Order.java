package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    private Order(Long id, Long totalAmount, Long userId, List<OrderLine> orderLines, Long discountAmount) {
        if (totalAmount == null || totalAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST , "총 가격은 0 이상이어야 합니다.");
        }

        if (discountAmount == null || discountAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST , "할인 금액은 0 이상이어야 합니다.");
        }

        if (orderLines == null || orderLines.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST , "주문 상품은 필수 입니다.");
        }

        if (totalAmount < discountAmount){
            throw new CoreException(ErrorType.BAD_REQUEST , "할인 금액이 총 가격보다 클 수 없습니다.");
        }

        this.id = id;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.orderLines = orderLines;
        this.discountAmount = discountAmount;
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
                .totalAmount(calculateTotalAmount(orderCommand.getOrderItems()))
                .discountAmount(orderCommand.getDiscountAmount())
                .orderLines(convert)
                .build();
    }

    private static Long calculateTotalAmount(List<OrderCommand.OrderItem> orderItems) {
        return orderItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public Long getFinalAmount() {
        return this.totalAmount - this.discountAmount;
    }
}
