// [LLD-REPO-01] StockReservationRepository — docs/lld/stock-reservation.md > 도메인 레이어 2-3
package com.loopers.domain.stock;

import java.util.List;

public interface StockReservationRepository {

    StockReservation save(StockReservation reservation);

    List<StockReservation> saveAll(List<StockReservation> reservations);

    List<StockReservation> findByOrderId(Long orderId);

    List<StockReservation> findByOrderIdForUpdate(Long orderId);
}
