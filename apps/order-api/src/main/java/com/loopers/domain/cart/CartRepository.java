package com.loopers.domain.cart;

import java.util.Optional;

import java.util.List;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findByUserId(Long userId);
    Optional<CartItem> findItemById(Long itemId);
    Optional<CartItem> findItemByIdAndUserId(Long itemId, Long userId);
    void deleteItem(CartItem item);
    void deleteCart(Cart cart);
    List<CartItem> findItemsByIds(List<Long> cartItemIds);
    List<CartItem> findItemsByIdsAndUserId(List<Long> cartItemIds, Long userId);
    List<CartItem> findItemsByUserId(Long userId);
}
