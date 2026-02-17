package com.loopers.domain.payment.idempotency;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"order_no", "order_key", "operation_type"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKey extends com.loopers.domain.BaseEntity {

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "order_key", nullable = false)
    private String orderKey;

    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Builder
    private IdempotencyKey(String orderNo, String orderKey, OperationType operationType, String resultData) {
        this.orderNo = orderNo;
        this.orderKey = orderKey;
        this.operationType = operationType;
        this.resultData = resultData;
    }

    public static IdempotencyKey create(String orderNo, String orderKey, OperationType operationType, String resultData) {
        return IdempotencyKey.builder()
                .orderNo(orderNo)
                .orderKey(orderKey)
                .operationType(operationType)
                .resultData(resultData)
                .build();
    }

    public enum OperationType {
        READY,
        PAYMENT_SESSION
    }
}
