package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemJpaRepository extends JpaRepository<CartItem, Long> {
}
