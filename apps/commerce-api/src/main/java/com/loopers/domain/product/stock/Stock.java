package com.loopers.domain.product.stock;

import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.ErrorType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Stock {
    private Long id;
    private Long productId;
    private Long quantity;

    @Builder
    private Stock(Long id, Long productId, Long quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static Stock create(Long productId, Long quantity) {
        return Stock.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    public void deduct(Long quantity) {
        if (this.quantity < quantity) {
            throw new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT);
        }
        this.quantity -= quantity;
    }
}
