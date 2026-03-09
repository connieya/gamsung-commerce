package com.loopers.interfaces.api.internal;

import java.util.List;

public class InternalDto {

    public record UserResponse(Long id, String userId, String email) {}

    public record ProductBulkRequest(List<Long> productIds) {}

    public record ProductResponse(Long id, String name, Long price, String imageUrl, String brandName) {}

    public record CouponDiscountRequest(Long userId, Long couponId, Long totalAmount) {}

    public record CouponDiscountResponse(Long discountAmount) {}

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
}
