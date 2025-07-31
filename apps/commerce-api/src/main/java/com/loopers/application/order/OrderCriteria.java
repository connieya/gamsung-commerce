package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.Product;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCriteria {

    private String userId;
    private List<OrderItem> orderItems;

    public OrderCriteria(String userId, List<OrderItem> orderItems) {
        this.userId = userId;
        this.orderItems = orderItems;
    }

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

    public OrderCommand toCommand(List<Product> products , Long userId) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

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
                .collect(Collectors.toList());

        return OrderCommand.of(userId, convertedItems, totalAmount);
    }
}
