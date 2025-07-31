package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.Product;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class OrderCriteria {

    private String userId;
    private List<OrderItem> orderItems;

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderItem {
        private final Long productId;
        private final Long quantity;
    }

    public List<Long> getProductIds() {
        return orderItems.stream().map(OrderItem::getProductId).collect(Collectors.toList());
    }

    public OrderCommand toCommand(List<Product> products) {
        Long totalAmount = products.stream().mapToLong(Product::getPrice).sum();
        return OrderCommand.of(userId, totalAmount);
    }
}
