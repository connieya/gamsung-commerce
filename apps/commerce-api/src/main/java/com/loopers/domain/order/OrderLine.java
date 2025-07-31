package com.loopers.domain.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderLine {

    private Long productId;
    private Long orderId;
    private Long quantity;
    private Long price;

    @Builder
    private OrderLine(Long productId, Long orderId, Long quantity, Long price) {
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderLine create(Long productId, Long quantity , Long price){
        return OrderLine
                .builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .build();
    }
}
