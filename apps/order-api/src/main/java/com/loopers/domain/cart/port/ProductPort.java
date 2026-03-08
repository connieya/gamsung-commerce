package com.loopers.domain.cart.port;

public interface ProductPort {
    ProductInfo getProduct(Long productId);

    record ProductInfo(Long id, String name, Long price, String imageUrl) {}
}
