package com.loopers.domain.product.stock;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Stock {

    private Long productId;
    private Long quantity;

    @Builder
    private Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static Stock create(Long productId, Long quantity) {
        return Stock.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}
