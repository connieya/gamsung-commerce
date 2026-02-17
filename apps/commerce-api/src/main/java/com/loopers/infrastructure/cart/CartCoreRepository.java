package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartItem;
import com.loopers.domain.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartCoreRepository implements CartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final CartItemJpaRepository cartItemJpaRepository;

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return cartJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<CartItem> findItemById(Long itemId) {
        return cartItemJpaRepository.findById(itemId);
    }

    @Override
    public void deleteItem(CartItem item) {
        cartItemJpaRepository.delete(item);
    }

    @Override
    public void deleteCart(Cart cart) {
        cartJpaRepository.delete(cart);
    }

    @Override
    public java.util.List<CartItem> findItemsByIds(java.util.List<Long> cartItemIds) {
        return cartItemJpaRepository.findAllById(cartItemIds);
    }
}
