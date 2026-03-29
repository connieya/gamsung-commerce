package com.loopers.domain.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Table(name = "orders")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Column(name = "ref_user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderLine> orderLines = new ArrayList<>();

    private Long discountAmount;

    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;


    @Builder
    private Order(Long totalAmount, Long userId, List<OrderLine> orderLines, Long discountAmount ,String orderNumber) {
        if (totalAmount == null || totalAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "총 가격은 0 이상이어야 합니다.");
        }

        if (discountAmount == null || discountAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 0 이상이어야 합니다.");
        }

        if (orderLines == null || orderLines.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상품은 필수 입니다.");
        }

        if (totalAmount < discountAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액이 총 가격보다 클 수 없습니다.");
        }
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.orderLines = orderLines;
        this.discountAmount = discountAmount;
        this.orderStatus = OrderStatus.INIT;
        this.orderNumber = orderNumber;
    }

    public static Order create(OrderCommand orderCommand) {
        return create(orderCommand, generateOrderNumber());
    }

    public static Order create(OrderCommand orderCommand, String orderNumber) {
        List<OrderLine> orderLines = orderCommand.getOrderItems()
                .stream()
                .map(item -> OrderLine.create(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList();

        Order order = Order
                .builder()
                .userId(orderCommand.getUserId())
                .totalAmount(calculateTotalAmount(orderCommand.getOrderItems()))
                .discountAmount(orderCommand.getDiscountAmount())
                .orderLines(orderLines)
                .orderNumber(orderNumber)
                .build();

        orderLines.forEach(orderLine -> orderLine.setOrder(order));

        return order;
    }


    public void paid() {
        this.orderStatus = OrderStatus.PAID;
    }

    public void cancel() {
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new OrderException.OrderInvalidStatusException(ErrorType.ORDER_INVALID_STATUS);
        }
        this.orderStatus = OrderStatus.CANCELED;
    }

    private static Long calculateTotalAmount(List<OrderCommand.OrderItem> orderItems) {
        return orderItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public Long getFinalAmount() {
        return this.totalAmount - this.discountAmount;
    }

    public void validatePay() {
        if (this.orderStatus != OrderStatus.INIT) {
            throw new OrderException.OrderInvalidStatusException(ErrorType.ORDER_INVALID_STATUS);
        }

        if (getFinalAmount() <= 0) {
            throw new OrderException.OrderInvalidAmountException(ErrorType.ORDER_INVALID_AMOUNT);
        }
    }

    private static String generateOrderNumber() {
        return OrderNumberGenerator.generate();
    }

}
