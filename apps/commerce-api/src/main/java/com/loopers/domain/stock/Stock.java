// [LLD-ENTITY-01] Stock — docs/lld/stock-reservation.md > 도메인 레이어 2-1
package com.loopers.domain.stock;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Column(name = "ref_sku_id", nullable = true)
    private Long skuId;

    private Long quantity;

    // [LLD-ENTITY-01] reservedQuantity 필드 — docs/lld/stock-reservation.md > 도메인 레이어 2-1
    @Column(name = "reserved_quantity", nullable = false)
    private Long reservedQuantity = 0L;


    @Builder
    private Stock(Long productId, Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 재고 수량은 0 초과 이어야 합니다.");
        }

        if (productId == null || productId < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 아이디가 올바르지 않습니다.");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = 0L;
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

    // [LLD-ENTITY-01] reserve() — docs/lld/stock-reservation.md > 도메인 레이어 2-1
    public void reserve(Long quantity) {
        long available = this.quantity - this.reservedQuantity;
        if (available < quantity) {
            throw new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT);
        }
        this.reservedQuantity += quantity;
    }

    // [LLD-ENTITY-01] releaseReservation() — docs/lld/stock-reservation.md > 도메인 레이어 2-1
    public void releaseReservation(Long quantity) {
        this.reservedQuantity -= quantity;
    }

    // [LLD-ENTITY-01] confirmReservation() — docs/lld/stock-reservation.md > 도메인 레이어 2-1
    public void confirmReservation(Long quantity) {
        this.quantity -= quantity;
        this.reservedQuantity -= quantity;
    }
}
