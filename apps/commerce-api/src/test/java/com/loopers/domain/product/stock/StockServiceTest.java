package com.loopers.domain.product.stock;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    StockService stockService;

    @Mock
    StockRepository stockRepository;

    @Test
    @DisplayName("주문한 수량만큼 재고를 차감한다. ")
    void deduct_success() {
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

        // when
        stockService.deduct(List.of(1L, 2L), OrderCommand.of(1L, List.of(orderItem1, orderItem2), 10000L));

        // then
        verify(stockRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("상품 재고보다 주문 수량이 많으면 InsufficientStockException 예외가 발생한다.")
    void deduct_fail() {
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

        // when
        Stock stock1 = Stock.create(1L, 15L);
        Stock stock2 = Stock.create(2L, 3L);
        when(stockRepository.findByProductIdIn(List.of(1L, 2L))).thenReturn(List.of(stock1, stock2));

        // then
        assertThatThrownBy(() -> {
            stockService.deduct(List.of(1L, 2L), OrderCommand.of(1L, List.of(orderItem1, orderItem2), 10000L));
        }).isInstanceOf(ProductException.InsufficientStockException.class);
    }
}
