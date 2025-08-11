package com.loopers.domain.stock;

import com.loopers.domain.common.Validatable;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Stock extends Validatable<Stock> {
    private Long id;
    @NotNull
    private Long productId;
    @Positive
    private Long quantity;

    @Builder
    private Stock(Long id, Long productId, Long quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static Stock create(Long productId, Long quantity) {
        Stock stock = Stock.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        stock.validate();

        return stock;
    }

    public void deduct(Long quantity) {
        if (this.quantity < quantity) {
            throw new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT);
        }
        this.quantity -= quantity;
    }
}
