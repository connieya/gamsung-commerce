package com.loopers.domain.cart;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Cart addItem(Long userId, Long productId, Long quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));

        // 같은 상품이 있으면 수량만 증가
        boolean updated = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .map(item -> {
                    item.updateQuantity(item.getQuantity() + quantity);
                    return true;
                })
                .orElse(false);

        if (!updated) {
            cart.addItem(CartItem.create(productId, quantity, product.getPrice()));
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
}
