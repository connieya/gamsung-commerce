package com.loopers.domain.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CartTest {

    @Nested
    @DisplayName("Cart 생성")
    class Create {

        @Test
        @DisplayName("userId로 생성하면 빈 아이템 목록을 가진다")
        void create_withUserId() {
            // given
            Long userId = 1L;

            // when
            Cart cart = Cart.create(userId);

            // then
            assertAll(
                    () -> assertThat(cart.getUserId()).isEqualTo(userId),
                    () -> assertThat(cart.getItems()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("아이템 추가")
    class AddItem {

        @Test
        @DisplayName("CartItem을 추가하면 items에 포함된다")
        void addItem_single() {
            // given
            Cart cart = Cart.create(1L);
            CartItem item = CartItem.create(100L, 2L, 5000L);

            // when
            cart.addItem(item);

            // then
            assertAll(
                    () -> assertThat(cart.getItems()).hasSize(1),
                    () -> assertThat(item.getCart()).isEqualTo(cart)
            );
        }

        @Test
        @DisplayName("여러 아이템을 추가하면 모두 포함된다")
        void addItem_multiple() {
            // given
            Cart cart = Cart.create(1L);
            CartItem item1 = CartItem.create(100L, 1L, 5000L);
            CartItem item2 = CartItem.create(200L, 2L, 10000L);
            CartItem item3 = CartItem.create(300L, 3L, 15000L);

            // when
            cart.addItem(item1);
            cart.addItem(item2);
            cart.addItem(item3);

            // then
            assertThat(cart.getItems()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("아이템 삭제")
    class RemoveItem {

        @Test
        @DisplayName("삭제하면 items에서 제거된다")
        void removeItem_success() {
            // given
            Cart cart = Cart.create(1L);
            CartItem item = CartItem.create(100L, 2L, 5000L);
            cart.addItem(item);

            // when
            cart.removeItem(item);

            // then
            assertAll(
                    () -> assertThat(cart.getItems()).isEmpty(),
                    () -> assertThat(item.getCart()).isNull()
            );
        }
    }

    @Nested
    @DisplayName("전체 비우기")
    class ClearItems {

        @Test
        @DisplayName("clearItems 호출 시 모든 아이템이 제거된다")
        void clearItems_success() {
            // given
            Cart cart = Cart.create(1L);
            cart.addItem(CartItem.create(100L, 1L, 5000L));
            cart.addItem(CartItem.create(200L, 2L, 10000L));

            // when
            cart.clearItems();

            // then
            assertThat(cart.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("총 금액 계산")
    class GetTotalAmount {

        @Test
        @DisplayName("price * quantity 합계를 반환한다")
        void getTotalAmount_withItems() {
            // given
            Cart cart = Cart.create(1L);
            cart.addItem(CartItem.create(100L, 2L, 5000L));   // 10000
            cart.addItem(CartItem.create(200L, 1L, 10000L));  // 10000

            // when
            Long totalAmount = cart.getTotalAmount();

            // then
            assertThat(totalAmount).isEqualTo(20000L);
        }

        @Test
        @DisplayName("빈 Cart는 0을 반환한다")
        void getTotalAmount_empty() {
            // given
            Cart cart = Cart.create(1L);

            // when
            Long totalAmount = cart.getTotalAmount();

            // then
            assertThat(totalAmount).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("CartItem")
    class CartItemTest {

        @Test
        @DisplayName("create, updateQuantity, getSubtotal이 정상 동작한다")
        void cartItem_operations() {
            // given
            CartItem item = CartItem.create(100L, 2L, 5000L);

            // then - create 검증
            assertAll(
                    () -> assertThat(item.getProductId()).isEqualTo(100L),
                    () -> assertThat(item.getQuantity()).isEqualTo(2L),
                    () -> assertThat(item.getPrice()).isEqualTo(5000L),
                    () -> assertThat(item.getSubtotal()).isEqualTo(10000L)
            );

            // when - updateQuantity
            item.updateQuantity(5L);

            // then
            assertAll(
                    () -> assertThat(item.getQuantity()).isEqualTo(5L),
                    () -> assertThat(item.getSubtotal()).isEqualTo(25000L)
            );
        }
    }
}
