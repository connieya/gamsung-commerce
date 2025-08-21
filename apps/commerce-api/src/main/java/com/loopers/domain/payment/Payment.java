package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "ref_order_id ", nullable = false)
    private Long orderId;

    @Column(name = "ref_user_id", nullable = false)
    private Long userId;

    @Column(name = "method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Builder
    private Payment(Long amount, Long orderId, Long userId, PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        this.amount = amount;
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public static Payment create(Long amount, Long orderId, Long userId , PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        return Payment
                .builder()
                .amount(amount)
                .orderId(orderId)
                .userId(userId)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();
    }
}
