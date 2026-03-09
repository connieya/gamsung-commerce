package com.loopers.domain.cart;

import com.loopers.domain.cart.port.ProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    CartService cartService;

    @Mock
    CartRepository cartRepository;

    @Mock
    ProductPort productPort;

    private Cart createCartWithItem(Long userId, Long itemId, Long productId, Long quantity, Long price) {
        Cart cart = Cart.create(userId);
        ReflectionTestUtils.setField(cart, "id", 1L);
        CartItem item = CartItem.create(productId, quantity, price);
        ReflectionTestUtils.setField(item, "id", itemId);
        cart.addItem(item);
        return cart;
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("장바구니가 없으면 새로 생성 후 아이템을 추가한다")
        void addItem_newCart() {
            // given
            Long userId = 1L;
            Long productId = 100L;
            Long quantity = 2L;
            ProductPort.ProductInfo productInfo = new ProductPort.ProductInfo(productId, "상품A", 5000L, "http://img.com/a.jpg");

            when(productPort.getProduct(productId)).thenReturn(productInfo);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Cart result = cartService.addItem(userId, productId, quantity);

            // then
            assertAll(
                    () -> assertThat(result.getItems()).hasSize(1),
                    () -> assertThat(result.getItems().get(0).getProductId()).isEqualTo(productId),
                    () -> assertThat(result.getItems().get(0).getQuantity()).isEqualTo(quantity),
                    () -> assertThat(result.getItems().get(0).getPrice()).isEqualTo(5000L)
            );
            verify(cartRepository, times(2)).save(any(Cart.class));
        }

        @Test
        @DisplayName("같은 상품이 있으면 수량을 증가시킨다")
        void addItem_existingProduct_increaseQuantity() {
            // given
            Long userId = 1L;
            Long productId = 100L;
            Long additionalQuantity = 3L;
            Cart cart = createCartWithItem(userId, 1L, productId, 2L, 5000L);

            ProductPort.ProductInfo productInfo = new ProductPort.ProductInfo(productId, "상품A", 5000L, "http://img.com/a.jpg");
            when(productPort.getProduct(productId)).thenReturn(productInfo);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Cart result = cartService.addItem(userId, productId, additionalQuantity);

            // then
            assertAll(
                    () -> assertThat(result.getItems()).hasSize(1),
                    () -> assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5L) // 2 + 3
            );
        }

        @Test
        @DisplayName("다른 상품이면 새 아이템을 추가한다")
        void addItem_differentProduct_addNew() {
            // given
            Long userId = 1L;
            Long existingProductId = 100L;
            Long newProductId = 200L;
            Cart cart = createCartWithItem(userId, 1L, existingProductId, 2L, 5000L);

            ProductPort.ProductInfo productInfo = new ProductPort.ProductInfo(newProductId, "상품B", 10000L, "http://img.com/b.jpg");
            when(productPort.getProduct(newProductId)).thenReturn(productInfo);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Cart result = cartService.addItem(userId, newProductId, 1L);

            // then
            assertThat(result.getItems()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("장바구니가 있으면 반환한다")
        void getCart_exists() {
            // given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

            // when
            Cart result = cartService.getCart(userId);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("장바구니가 없으면 빈 Cart를 반환한다")
        void getCart_notExists() {
            // given
            Long userId = 1L;
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when
            Cart result = cartService.getCart(userId);

            // then
            assertAll(
                    () -> assertThat(result.getUserId()).isEqualTo(userId),
                    () -> assertThat(result.getItems()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("updateItemQuantity")
    class UpdateItemQuantity {

        @Test
        @DisplayName("아이템이 있으면 수량을 변경한다")
        void updateItemQuantity_success() {
            // given
            Long itemId = 1L;
            Long newQuantity = 5L;
            CartItem item = CartItem.create(100L, 2L, 5000L);
            ReflectionTestUtils.setField(item, "id", itemId);

            when(cartRepository.findItemById(itemId)).thenReturn(Optional.of(item));

            // when
            cartService.updateItemQuantity(itemId, newQuantity);

            // then
            assertThat(item.getQuantity()).isEqualTo(newQuantity);
        }

        @Test
        @DisplayName("아이템이 없으면 IllegalArgumentException이 발생한다")
        void updateItemQuantity_notFound() {
            // given
            Long itemId = 999L;
            when(cartRepository.findItemById(itemId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.updateItemQuantity(itemId, 5L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("아이템이 있으면 삭제한다")
        void removeItem_success() {
            // given
            Long userId = 1L;
            Long itemId = 1L;
            CartItem item = CartItem.create(100L, 2L, 5000L);
            ReflectionTestUtils.setField(item, "id", itemId);

            when(cartRepository.findItemById(itemId)).thenReturn(Optional.of(item));

            // when
            cartService.removeItem(userId, itemId);

            // then
            verify(cartRepository).deleteItem(item);
        }

        @Test
        @DisplayName("아이템이 없으면 IllegalArgumentException이 발생한다")
        void removeItem_notFound() {
            // given
            Long userId = 1L;
            Long itemId = 999L;
            when(cartRepository.findItemById(itemId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.removeItem(userId, itemId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("clearCart")
    class ClearCart {

        @Test
        @DisplayName("장바구니가 있으면 삭제한다")
        void clearCart_exists() {
            // given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

            // when
            cartService.clearCart(userId);

            // then
            verify(cartRepository).deleteCart(cart);
        }

        @Test
        @DisplayName("장바구니가 없으면 아무 동작도 하지 않는다")
        void clearCart_notExists() {
            // given
            Long userId = 1L;
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when
            cartService.clearCart(userId);

            // then
            verify(cartRepository, never()).deleteCart(any());
        }
    }

    @Nested
    @DisplayName("getCartCount")
    class GetCartCount {

        @Test
        @DisplayName("장바구니 아이템의 수량 합계를 반환한다")
        void getCartCount_withItems() {
            // given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cart.addItem(CartItem.create(100L, 2L, 5000L));
            cart.addItem(CartItem.create(200L, 3L, 10000L));

            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

            // when
            Long count = cartService.getCartCount(userId);

            // then
            assertThat(count).isEqualTo(5L); // 2 + 3
        }

        @Test
        @DisplayName("장바구니가 없으면 0을 반환한다")
        void getCartCount_notExists() {
            // given
            Long userId = 1L;
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when
            Long count = cartService.getCartCount(userId);

            // then
            assertThat(count).isEqualTo(0L);
        }
    }
}
