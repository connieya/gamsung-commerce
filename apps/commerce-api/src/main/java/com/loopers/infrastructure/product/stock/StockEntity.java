package com.loopers.infrastructure.product.stock;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.stock.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock")
public class StockEntity extends BaseEntity {

    private Long productId;
    private Long quantity;

    public static StockEntity from(Stock stock) {
        StockEntity stockEntity = new StockEntity();

        stockEntity.productId = stock.getProductId();
        stockEntity.quantity = stock.getQuantity();

        return stockEntity;
    }

    public Stock toDomain() {
        return Stock
                .builder()
                .id(id)
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
