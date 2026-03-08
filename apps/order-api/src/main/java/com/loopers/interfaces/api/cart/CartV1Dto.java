package com.loopers.interfaces.api.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartItem;

import java.util.List;

public class CartV1Dto {

    public static class Request {
        public record AddItem(Long productId, Long quantity) {}
        public record UpdateQuantity(Long quantity) {}
    }

    public static class Response {
        public record CartDetail(
                Long cartId,
                List<CartItemDto> items,
                Long totalAmount
        ) {
            public static CartDetail from(Cart cart) {
                List<CartItemDto> items = cart.getItems().stream()
                        .map(CartItemDto::from)
                        .toList();
                return new CartDetail(cart.getId(), items, cart.getTotalAmount());
            }
        }

        public record CartCount(Long count) {}
    }

    public record CartItemDto(
            Long itemId,
            Long productId,
            Long quantity,
            Long price,
            Long subtotal
    ) {
        public static CartItemDto from(CartItem item) {
            return new CartItemDto(
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getSubtotal()
            );
        }
    }
}
