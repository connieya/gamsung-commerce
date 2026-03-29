// [LLD-CMD-01] StockCommand — docs/lld/stock-reservation.md > 도메인 레이어 2-4
package com.loopers.domain.stock;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class StockCommand {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class DeductStocks {
        private List<Item> items;


        @Builder
        private DeductStocks(List<Item> items) {
            this.items = items;
        }

        @Getter
        @Builder
        public static class Item {
            private Long productId;
            private Long quantity;
        }

        public static DeductStocks create(List<Item> items) {
            return DeductStocks
                    .builder()
                    .items(items)
                    .build();

        }
    }

    // [LLD-CMD-01] ReserveStocks — docs/lld/stock-reservation.md > 도메인 레이어 2-4
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReserveStocks {
        private Long orderId;
        private List<Item> items;

        @Getter
        @Builder
        public static class Item {
            private final Long productId;
            private final Long quantity;
        }

        @Builder
        private ReserveStocks(Long orderId, List<Item> items) {
            this.orderId = orderId;
            this.items = items;
        }

        public static ReserveStocks of(Long orderId, List<Item> items) {
            return ReserveStocks.builder()
                    .orderId(orderId)
                    .items(items)
                    .build();
        }
    }

    // [LLD-CMD-01] ConfirmReservation — docs/lld/stock-reservation.md > 도메인 레이어 2-4
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConfirmReservation {
        private Long orderId;

        @Builder
        private ConfirmReservation(Long orderId) {
            this.orderId = orderId;
        }

        public static ConfirmReservation of(Long orderId) {
            return ConfirmReservation.builder()
                    .orderId(orderId)
                    .build();
        }
    }

    // [LLD-CMD-01] CancelReservation — docs/lld/stock-reservation.md > 도메인 레이어 2-4
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CancelReservation {
        private Long orderId;

        @Builder
        private CancelReservation(Long orderId) {
            this.orderId = orderId;
        }

        public static CancelReservation of(Long orderId) {
            return CancelReservation.builder()
                    .orderId(orderId)
                    .build();
        }
    }
}
