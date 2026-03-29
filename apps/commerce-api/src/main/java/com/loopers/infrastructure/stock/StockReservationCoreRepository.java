// [LLD-INFRA-02] StockReservationCoreRepository — docs/lld/stock-reservation.md > 인프라 레이어 3-2
package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.StockReservation;
import com.loopers.domain.stock.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockReservationCoreRepository implements StockReservationRepository {

    private final StockReservationJpaRepository jpaRepository;

    @Override
    public StockReservation save(StockReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public List<StockReservation> saveAll(List<StockReservation> reservations) {
        return jpaRepository.saveAll(reservations);
    }

    @Override
    public List<StockReservation> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<StockReservation> findByOrderIdForUpdate(Long orderId) {
        return jpaRepository.findByOrderIdForUpdate(orderId);
    }
}
