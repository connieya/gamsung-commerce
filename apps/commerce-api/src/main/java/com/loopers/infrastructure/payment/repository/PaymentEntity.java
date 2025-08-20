package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "payment")
@Getter
public class PaymentEntity extends BaseEntity {

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "ref_order_id ", nullable = false)
    private Long orderId;

    @Column(name = "ref_user_id", nullable = false)
    private Long userId;

    @Column(name = "method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;

    public static PaymentEntity fromDomain(Payment payment) {
        PaymentEntity paymentEntity = new PaymentEntity();

        paymentEntity.amount = payment.getAmount();
        paymentEntity.orderId = payment.getOrderId();
        paymentEntity.userId = payment.getUserId();
        paymentEntity.paymentMethod = payment.getPaymentMethod();
        paymentEntity.paymentStatus = payment.getPaymentStatus();

        return paymentEntity;
    }

    public Payment toDomain() {
        return Payment
                .builder()
                .id(id)
                .amount(amount)
                .userId(userId)
                .orderId(orderId)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();
    }
}
