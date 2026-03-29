// [LLD-ENTITY-02] StockReservation — docs/lld/stock-reservation.md > 도메인 레이어 2-2
package com.loopers.domain.stock;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservation extends BaseEntity {

    @Column(name = "ref_stock_id", nullable = false)
    private Long stockId;

    @Column(name = "ref_order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    public enum ReservationStatus {
        PENDING, CONFIRMED, CANCELLED
    }

    public static StockReservation create(Long stockId, Long orderId, Long quantity) {
        StockReservation r = new StockReservation();
        r.stockId = stockId;
        r.orderId = orderId;
        r.quantity = quantity;
        r.status = ReservationStatus.PENDING;
        return r;
    }

    // [LLD-ENTITY-02] confirm() — docs/lld/stock-reservation.md > 도메인 레이어 2-2
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    // [LLD-ENTITY-02] cancel() — docs/lld/stock-reservation.md > 도메인 레이어 2-2
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
