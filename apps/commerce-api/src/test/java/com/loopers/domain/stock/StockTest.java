// [LLD-TEST-01] StockTest — docs/lld/stock-reservation.md > 테스트 전략 > 단위 테스트
package com.loopers.domain.stock;

import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {


    @DisplayName("1개 미만 정수로 재고 생성 시 실패한다.")
    @ParameterizedTest
    @ValueSource(longs = {
            0L,
            -1L
    })
    void createFail(Long value){
        // when & then
        assertThatThrownBy(() -> Stock.create(1L, value))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);;
    }


    @Test
    @DisplayName("주문한 수량만큼 재고를 차감한다.")
    void deduct_Success() {
        // given
        Stock stock = Stock.create(1L, 100L);

        // when
        stock.deduct(5L);

        // then
        assertThat(stock.getQuantity()).isEqualTo(95L);
    }

    @Test
    @DisplayName("주문한 수량보다 상품 재고가 작으면 InsufficientStockException 예외가 발생한다.")
    void deduct_Fail() {
        // given
        Stock stock = Stock.create(1L, 10L);

        // when & then
        assertThatThrownBy(() -> stock.deduct(15L))
                .isInstanceOf(ProductException.InsufficientStockException.class);
    }

    // [LLD-TEST-01] reserve_성공 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("가용 재고 범위 내에서 선점하면 reservedQuantity가 증가한다.")
    void reserve_Success() {
        // given: quantity=10, reserved=0
        Stock stock = Stock.create(1L, 10L);

        // when: reserve(5)
        stock.reserve(5L);

        // then: reserved=5
        assertThat(stock.getReservedQuantity()).isEqualTo(5L);
    }

    // [LLD-TEST-01] reserve_실패_재고부족 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("가용 재고보다 많이 선점 요청 시 InsufficientStockException 예외가 발생한다.")
    void reserve_Fail_InsufficientStock() {
        // given: quantity=10, reserved=8 → available=2
        Stock stock = Stock.create(1L, 10L);
        stock.reserve(8L);

        // when & then: reserve(5) → 실패
        assertThatThrownBy(() -> stock.reserve(5L))
                .isInstanceOf(ProductException.InsufficientStockException.class);
    }

    // [LLD-TEST-01] releaseReservation_성공 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("선점 해제 시 reservedQuantity가 감소한다.")
    void releaseReservation_Success() {
        // given: reserved=5
        Stock stock = Stock.create(1L, 10L);
        stock.reserve(5L);

        // when: release(5)
        stock.releaseReservation(5L);

        // then: reserved=0
        assertThat(stock.getReservedQuantity()).isEqualTo(0L);
    }

    // [LLD-TEST-01] confirmReservation_성공 — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("예약 확정 시 quantity가 차감되고 reservedQuantity도 감소한다.")
    void confirmReservation_Success() {
        // given: quantity=10, reserved=3
        Stock stock = Stock.create(1L, 10L);
        stock.reserve(3L);

        // when: confirm(3)
        stock.confirmReservation(3L);

        // then: quantity=7, reserved=0
        assertThat(stock.getQuantity()).isEqualTo(7L);
        assertThat(stock.getReservedQuantity()).isEqualTo(0L);
    }
}
