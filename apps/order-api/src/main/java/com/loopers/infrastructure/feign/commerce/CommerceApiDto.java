// [LLD-FEIGN-02] CommerceApiDto — docs/lld/stock-reservation.md > order-api 연동 6-2
package com.loopers.infrastructure.feign.commerce;

import java.util.List;

public class CommerceApiDto {

    public record UserResponse(Long id, String userId, String email) {}

    public record ProductBulkRequest(List<Long> productIds) {}

    public record ProductResponse(Long id, String name, Long price, String imageUrl, String brandName) {}

    public record PaymentReadyRequest(
            Long orderId,
            String orderNumber,
            Long userId,
            Long amount,
            String paymentMethod,
            String payKind,
            String orderKey
    ) {}

    public record PaymentReadyResponse(Long paymentId, String paymentStatus) {}

    // [LLD-FEIGN-02] StockReserveRequest — docs/lld/stock-reservation.md > order-api 연동 6-2
    public record StockReserveRequest(
            Long orderId,
            List<StockItem> items
    ) {
        public record StockItem(Long productId, Long quantity) {}
    }

    // [LLD-FEIGN-02] StockReserveResponse — docs/lld/stock-reservation.md > order-api 연동 6-2
    public record StockReserveResponse(Long orderId, String status) {}

    // [LLD-FEIGN-02] StockCancelRequest — docs/lld/stock-reservation.md > order-api 연동 6-2
    public record StockCancelRequest(Long orderId) {}
}
