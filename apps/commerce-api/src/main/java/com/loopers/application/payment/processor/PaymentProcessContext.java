package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.domain.payment.CardType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentProcessContext {

    private Long orderId;
    private String userId;
    private String cardNumber;
    private CardType cardType;
    private Long couponId;

    @Builder
    private PaymentProcessContext(Long orderId, String userId, String cardNumber, CardType cardType ,Long couponId) {
        this.orderId = orderId;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.couponId = couponId;
    }

    public static PaymentProcessContext of(PaymentCriteria.Pay payCriteria) {
        return PaymentProcessContext.builder()
                .orderId(payCriteria.orderId())
                .userId(payCriteria.userId())
                .cardNumber(payCriteria.cardNumber())
                .cardType(payCriteria.cardType())
                .couponId(payCriteria.couponId())
                .build();

    }
}
