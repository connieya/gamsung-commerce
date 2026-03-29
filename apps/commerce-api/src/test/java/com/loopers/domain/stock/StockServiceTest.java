// [LLD-TEST-03] StockServiceTest — docs/lld/stock-reservation.md > 테스트 전략 > 서비스 테스트
package com.loopers.domain.stock;

import com.loopers.domain.product.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    StockService stockService;

    @Mock
    StockRepository stockRepository;

    @Mock
    StockReservationRepository reservationRepository;

    // [LLD-TEST-03] reserve_성공 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("재고 선점 성공 시 saveAll이 호출된다.")
    void reserve_success() {
        // given
        StockCommand.ReserveStocks.Item item1 = StockCommand.ReserveStocks.Item.builder()
                .productId(1L)
                .quantity(5L)
                .build();
        StockCommand.ReserveStocks.Item item2 = StockCommand.ReserveStocks.Item.builder()
                .productId(2L)
                .quantity(3L)
                .build();
        StockCommand.ReserveStocks command = StockCommand.ReserveStocks.of(100L, List.of(item1, item2));

        Stock stock1 = Stock.create(1L, 10L);
        Stock stock2 = Stock.create(2L, 10L);
        when(stockRepository.findStocksForUpdate(List.of(1L, 2L))).thenReturn(List.of(stock1, stock2));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock1, stock2));
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of());

        // when
        stockService.reserve(command);

        // then
        verify(stockRepository, times(1)).saveAll(anyList());
        verify(reservationRepository, times(1)).saveAll(anyList());
    }

    // [LLD-TEST-03] reserve_실패_재고부족 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("재고 부족 시 InsufficientStockException이 전파된다.")
    void reserve_fail_insufficientStock() {
        // given
        StockCommand.ReserveStocks.Item item = StockCommand.ReserveStocks.Item.builder()
                .productId(1L)
                .quantity(15L)
                .build();
        StockCommand.ReserveStocks command = StockCommand.ReserveStocks.of(100L, List.of(item));

        Stock stock = Stock.create(1L, 10L);
        when(stockRepository.findStocksForUpdate(List.of(1L))).thenReturn(List.of(stock));

        // when & then
        assertThatThrownBy(() -> stockService.reserve(command))
                .isInstanceOf(ProductException.InsufficientStockException.class);
    }

    // [LLD-TEST-03] confirm_성공 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("결제 완료 시 예약이 CONFIRMED로 전이되고 배치 조회가 수행된다.")
    void confirm_success() {
        // given
        StockCommand.ConfirmReservation command = StockCommand.ConfirmReservation.of(100L);

        StockReservation reservation = StockReservation.create(1L, 100L, 3L);
        Stock stock = Stock.create(1L, 10L);
        stock.reserve(3L);

        when(reservationRepository.findByOrderIdForUpdate(100L)).thenReturn(List.of(reservation));
        when(stockRepository.findStocksForUpdateByIds(List.of(1L))).thenReturn(List.of(stock));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock));
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of(reservation));

        // when
        stockService.confirm(command);

        // then: 배치 조회가 수행됨 (N+1 없이)
        verify(stockRepository, times(1)).findStocksForUpdateByIds(anyList());
        verify(stockRepository, times(1)).saveAll(anyList());
        verify(reservationRepository, times(1)).saveAll(anyList());
        assertThat(reservation.getStatus()).isEqualTo(StockReservation.ReservationStatus.CONFIRMED);
    }

    // [LLD-TEST-03] cancel_성공 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("주문 취소 시 예약이 CANCELLED로 전이된다.")
    void cancel_success() {
        // given
        StockCommand.CancelReservation command = StockCommand.CancelReservation.of(100L);

        StockReservation reservation = StockReservation.create(1L, 100L, 3L);
        Stock stock = Stock.create(1L, 10L);
        stock.reserve(3L);

        when(reservationRepository.findByOrderIdForUpdate(100L)).thenReturn(List.of(reservation));
        when(stockRepository.findStocksForUpdateByIds(List.of(1L))).thenReturn(List.of(stock));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock));
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of(reservation));

        // when
        stockService.cancel(command);

        // then
        verify(stockRepository, times(1)).findStocksForUpdateByIds(anyList());
        assertThat(reservation.getStatus()).isEqualTo(StockReservation.ReservationStatus.CANCELLED);
    }
}
