package com.loopers.domain.cart;

import com.loopers.domain.cart.port.ProductPort;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductPort productPort;

    @Transactional
    public Cart addItem(Long userId, Long productId, Long quantity) {
        ProductPort.ProductInfo product = productPort.getProduct(productId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));

        boolean updated = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .map(item -> {
                    item.updateQuantity(item.getQuantity() + quantity);
                    return true;
                })
                .orElse(false);

        if (!updated) {
            cart.addItem(CartItem.create(productId, quantity, product.price()));
        }

        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.create(userId));
    }

    @Transactional
    public void updateItemQuantity(Long itemId, Long quantity) {
        CartItem item = cartRepository.findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다"));
        item.updateQuantity(quantity);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem item = cartRepository.findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다"));
        cartRepository.deleteItem(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId)
                .ifPresent(cartRepository::deleteCart);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findItemsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByIds(List<Long> cartItemIds, Long userId) {
        return cartRepository.findItemsByIdsAndUserId(cartItemIds, userId);
    }

    @Transactional(readOnly = true)
    public Long getCartCount(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.create(userId));
        return cart.getItems().stream()
                .mapToLong(CartItem::getQuantity)
                .sum();
    }
}
