package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
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

    private Long totalAmount;

    @Column(name = "ref_user_id" , nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    private Long discountAmount;

    @Column(name = "order_status" , nullable = false)
    private OrderStatus orderStatus;


    @Builder
    private Order(Long totalAmount, Long userId, List<OrderLine> orderLines, Long discountAmount) {
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
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.orderLines = orderLines;
        this.discountAmount = discountAmount;
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
    }

    public static Order fromDomain(Order order) {
        Order orderEntity = new Order();

        orderEntity.totalAmount = order.getTotalAmount();
        orderEntity.userId = order.getUserId();
        orderEntity.discountAmount = order.getDiscountAmount();
        orderEntity.orderStatus = order.getOrderStatus();


        // 연관관계 주입
        order.getOrderLines().forEach(orderLine -> {
            OrderLine lineEntity = OrderLine.fromDomain(orderLine, orderEntity);
            orderEntity.orderLines.add(lineEntity);
        });

        return orderEntity;
    }

    public static Order create(OrderCommand orderCommand) {

        return Order
                .builder()
                .userId(orderCommand.getUserId())
                .totalAmount(calculateTotalAmount(orderCommand.getOrderItems()))
                .discountAmount(orderCommand.getDiscountAmount())
                .orderLines(orderCommand.getOrderItems()
                        .stream()
                        .map(item ->
                                OrderLine.create(item.getProductId(), item.getQuantity(), item.getPrice())
                        ).toList())
                .build();
    }


    public void complete() {
        this.orderStatus = OrderStatus.PAYMENT_COMPLETED;
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
