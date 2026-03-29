// [LLD-TEST-02] StockReservationTest — docs/lld/stock-reservation.md > 테스트 전략 > 단위 테스트
package com.loopers.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockReservationTest {

    // [LLD-TEST-02] create: status=PENDING — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("예약 생성 시 상태가 PENDING이다.")
    void create_StatusIsPending() {
        // when
        StockReservation reservation = StockReservation.create(1L, 100L, 3L);

        // then
        assertThat(reservation.getStatus()).isEqualTo(StockReservation.ReservationStatus.PENDING);
        assertThat(reservation.getStockId()).isEqualTo(1L);
        assertThat(reservation.getOrderId()).isEqualTo(100L);
        assertThat(reservation.getQuantity()).isEqualTo(3L);
    }

    // [LLD-TEST-02] confirm: PENDING -> CONFIRMED — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("confirm() 호출 시 상태가 CONFIRMED로 전이된다.")
    void confirm_StatusIsConfirmed() {
        // given
        StockReservation reservation = StockReservation.create(1L, 100L, 3L);

        // when
        reservation.confirm();

        // then
        assertThat(reservation.getStatus()).isEqualTo(StockReservation.ReservationStatus.CONFIRMED);
    }

    // [LLD-TEST-02] cancel: PENDING -> CANCELLED — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("cancel() 호출 시 상태가 CANCELLED로 전이된다.")
    void cancel_StatusIsCancelled() {
        // given
        StockReservation reservation = StockReservation.create(1L, 100L, 3L);

        // when
        reservation.cancel();

        // then
        assertThat(reservation.getStatus()).isEqualTo(StockReservation.ReservationStatus.CANCELLED);
    }
}
