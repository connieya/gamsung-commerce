package com.loopers.domain.payment;

public class PaymentInfo {

    public record ReadyResult(Long paymentId, PaymentStatus paymentStatus) {}

    public record SessionResult(
            String orderNo,
            String paymentKey,
            Long amount,
            String paymentUrl,
            String pgKind
    ) {}
}
