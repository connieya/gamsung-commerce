package com.loopers.domain.cart;

import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findByUserId(Long userId);
    Optional<CartItem> findItemById(Long itemId);
    void deleteItem(CartItem item);
    void deleteCart(Cart cart);
}
