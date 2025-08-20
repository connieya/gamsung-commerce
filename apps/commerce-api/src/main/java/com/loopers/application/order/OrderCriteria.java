package com.loopers.application.order;

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
    private Long couponId;

    @Builder
    public OrderCriteria(String userId, List<OrderItem> orderItems, Long couponId) {
        this.userId = userId;
        this.orderItems = orderItems;
        this.couponId = couponId;
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

    public Long getTotalAmount(List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        return orderItems.stream()
                .mapToLong(item -> {
                    Product product = productMap.get(item.getProductId());
                    return product.getPrice() * item.getQuantity();
                }).sum();
    }

}
