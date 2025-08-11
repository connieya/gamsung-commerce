package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @DisplayName("주문 생성")
    @Nested
    class Create {

        @DisplayName("총 가격이 유효하지 않으면 , CoreException 이 발생한다.")
        @Test
        void throwException_withInvalidTotalPrice() {
            OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem
                    .builder()
                    .productId(1L)
                    .quantity(10L)
                    .price(-100L)
                    .build();

            OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem
                    .builder()
                    .productId(2L)
                    .quantity(5L)
                    .price(-100L)
                    .build();

            OrderCommand orderCommand = OrderCommand.of(1L, List.of(orderItem1, orderItem2), 10000L);

            // when
            assertThatThrownBy(
                    ()-> {
                        Order.create(orderCommand);
                    }
            ).isInstanceOf(CoreException.class);

        }

        @Test
        void create() {
            // given
            OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem
                    .builder()
                    .productId(1L)
                    .quantity(10L)
                    .price(2000L)
                    .build();

            OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem
                    .builder()
                    .productId(2L)
                    .quantity(5L)
                    .price(10000L)
                    .build();

            OrderCommand orderCommand = OrderCommand.of(1L, List.of(orderItem1, orderItem2), 10000L);

            // when
            Order order = Order.create(orderCommand);

            // then
            assertAll(
                    () -> assertThat(order.getDiscountAmount()).isEqualTo(10000L),
                    () -> assertThat(order.getTotalAmount()).isEqualTo(70000L),
                    () -> assertThat(order.getUserId()).isEqualTo(1L),
                    () -> assertThat(order.getOrderLines()).hasSize(2)
                            .extracting("productId", "quantity", "price")
                            .containsExactlyInAnyOrder(
                                    tuple(1L, 10L, 2000L),
                                    tuple(2L, 5L, 10000L)
                            )
            );
        }
    }


}
