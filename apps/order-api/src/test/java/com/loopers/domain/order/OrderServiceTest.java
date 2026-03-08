package com.loopers.domain.order;

import com.loopers.domain.order.exception.OrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.place(orderCommand);

        // then
        verify(orderRepository, times(1)).save(any(Order.class));

    }

    @Test
    @DisplayName("단일 상세 주문 조회")
    void getOrderDetail() {
        // given
        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem
                .builder()
                .productId(1L)
                .quantity(10L)
                .price(500L)
                .build();

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem
                .builder()
                .productId(2L)
                .quantity(5L)
                .price(2000L)
                .build();

        OrderCommand orderCommand = OrderCommand.of(1L, List.of(orderItem1, orderItem2), 15000L);

        Order order = Order.create(orderCommand);
        // when
        when(orderRepository.findOrderDetailById(1L)).thenReturn(Optional.of(order));
        OrderInfo orderInfo = orderService.getOrderDetail(1L);

        // then
        assertAll(

                () -> assertThat(orderInfo.getTotalAmount()).isEqualTo(15000L),
                () -> assertThat(order.getOrderLines()).hasSize(2)
                        .extracting("productId", "quantity", "orderPrice")
                        .containsExactlyInAnyOrder(
                                tuple(1L, 10L, 500L),
                                tuple(2L, 5L, 2000L)
                        )
        );
    }

    @Test
    @DisplayName("단일 주문 상세 조회시 존재하지 않는 주문을 조회하면 OrderNotFoundException 예외가 발생한다.")
    void getOrderDetail_fail() {
        // given

        // when
        when(orderRepository.findOrderDetailById(1L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> orderService.getOrderDetail(1L))
                .isInstanceOf(OrderException.OrderNotFoundException.class);
    }

}
