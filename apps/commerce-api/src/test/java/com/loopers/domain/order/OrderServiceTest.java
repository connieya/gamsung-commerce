package com.loopers.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Test
    @DisplayName("주문을 생성한다.")
    void place() {
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
        orderService.place(orderCommand);

        // then
        verify(orderRepository, times(1)).save(any(Order.class));

    }

}
