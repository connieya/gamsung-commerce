// [LLD-INFRA-01] StockReservationJpaRepository — docs/lld/stock-reservation.md > 인프라 레이어 3-1
package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.StockReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockReservationJpaRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM StockReservation r WHERE r.orderId = :orderId")
    List<StockReservation> findByOrderIdForUpdate(@Param("orderId") Long orderId);
}
