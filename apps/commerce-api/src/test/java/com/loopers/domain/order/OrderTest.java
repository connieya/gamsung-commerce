package com.loopers.domain.order;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {


    @Test
    void create() {
        // given
        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem
                .builder()
                .productId(1L)
                .quantity(10L)
                .price(5000L)
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
                () -> assertThat(order.getTotalAmount()).isEqualTo(10000L),
                () -> assertThat(order.getUserId()).isEqualTo(1L),
                () -> assertThat(order.getOrderLines()).hasSize(2)
                        .extracting("productId", "quantity", "price")
                        .containsExactlyInAnyOrder(
                                tuple(1L, 10L, 5000L),
                                tuple(2L, 5L, 10000L)
                        )
        );
    }

}
