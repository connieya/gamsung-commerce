// [LLD-DTO-01] StockInternalV1Dto — docs/lld/stock-reservation.md > API 레이어 5-1
package com.loopers.interfaces.api.stock;

import com.loopers.domain.stock.StockCommand;

import java.util.List;

public class StockInternalV1Dto {

    // [LLD-DTO-01] ReserveRequest — docs/lld/stock-reservation.md > API 레이어 5-1
    public record ReserveRequest(
            Long orderId,
            List<Item> items
    ) {
        public record Item(Long productId, Long quantity) {}

        public StockCommand.ReserveStocks toCommand() {
            List<StockCommand.ReserveStocks.Item> commandItems = items.stream()
                    .map(i -> StockCommand.ReserveStocks.Item.builder()
                            .productId(i.productId())
                            .quantity(i.quantity())
                            .build())
                    .toList();
            return StockCommand.ReserveStocks.of(orderId, commandItems);
        }
    }

    // [LLD-DTO-01] CancelRequest — docs/lld/stock-reservation.md > API 레이어 5-1
    public record CancelRequest(Long orderId) {
        public StockCommand.CancelReservation toCommand() {
            return StockCommand.CancelReservation.of(orderId);
        }
    }

    // [LLD-DTO-01] ReserveResponse — docs/lld/stock-reservation.md > API 레이어 5-1
    public record ReserveResponse(Long orderId, String status) {
        public static ReserveResponse of(Long orderId) {
            return new ReserveResponse(orderId, "PENDING");
        }
    }
}
