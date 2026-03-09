package com.loopers.application.review;

public interface OrderVerifier {

    void verifyPurchase(Long orderId, Long userId, Long productId);
}
