package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "issued_order_no", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"order_no"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssuedOrderNo extends BaseEntity {

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "order_signature", nullable = false, length = 512)
    private String orderSignature;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Column(name = "order_verify_key", nullable = false)
    private String orderVerifyKey;

    @Column(name = "order_key", nullable = false)
    private String orderKey;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Builder
    private IssuedOrderNo(String orderNo, String orderSignature, long timestamp, String orderVerifyKey, String orderKey) {
        this.orderNo = orderNo;
        this.orderSignature = orderSignature;
        this.timestamp = timestamp;
        this.orderVerifyKey = orderVerifyKey;
        this.orderKey = orderKey;
        this.used = false;
    }

    public static IssuedOrderNo create(OrderNoIssue issue) {
        return IssuedOrderNo.builder()
                .orderNo(issue.orderNo())
                .orderSignature(issue.orderSignature())
                .timestamp(issue.timestamp())
                .orderVerifyKey(issue.orderVerifyKey())
                .orderKey(issue.orderKey())
                .build();
    }

    public void markUsed() {
        this.used = true;
    }
}
