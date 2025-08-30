package com.loopers.domain.payment.attempt;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_attempt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAttempt extends BaseEntity {

    @Column(name = "transaction_key")
    private String transactionKey;

    @Column(name = "ref_payment_id" , nullable = false)
    private Long paymentId;

    @Column(name = "ref_order_number" , nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private AttemptStatus attemptStatus;


    @Builder
    private PaymentAttempt(String transactionKey, Long paymentId, String orderNumber, AttemptStatus attemptStatus) {
        this.transactionKey = transactionKey;
        this.paymentId = paymentId;
        this.orderNumber = orderNumber;
        this.attemptStatus = attemptStatus;
    }


    public static PaymentAttempt create(String transactionKey , Long paymentId , String orderNumber , AttemptStatus attemptStatus) {
        return PaymentAttempt
                .builder()
                .transactionKey(transactionKey)
                .paymentId(paymentId)
                .orderNumber(orderNumber)
                .attemptStatus(attemptStatus)
                .build();
    }


    public static PaymentAttempt create(Long paymentId , String orderNumber , AttemptStatus attemptStatus) {
        return PaymentAttempt
                .builder()
                .paymentId(paymentId)
                .orderNumber(orderNumber)
                .attemptStatus(attemptStatus)
                .build();
    }
}
