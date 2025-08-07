package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderCommandMapper {
    public static OrderCommand map(Long userId , OrderCriteria orderCriteria , List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderCriteria.OrderItem> orderItems = orderCriteria.getOrderItems();

        Long totalAmount = orderItems.stream()
                .mapToLong(item -> {
                    Product product = productMap.get(item.getProductId());
                    return product.getPrice() * item.getQuantity();
                }).sum();

        List<OrderCommand.OrderItem> convertedItems = orderItems.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    return OrderCommand.OrderItem.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(product.getPrice())
                            .build();
                })
                .toList();

        return OrderCommand.of(userId, convertedItems,totalAmount);

    }
}
