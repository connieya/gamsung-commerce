package com.loopers.domain.cart;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "cart_item")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Builder
    private CartItem(Long productId, Long quantity, Long price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public static CartItem create(Long productId, Long quantity, Long price) {
        return CartItem.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .build();
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void updateQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getSubtotal() {
        return price * quantity;
    }
}
