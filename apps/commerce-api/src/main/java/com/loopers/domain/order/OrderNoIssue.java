package com.loopers.domain.order;

public record OrderNoIssue(
        String orderNo,
        String orderSignature,
        long timestamp,
        String orderVerifyKey,
        String orderKey
) {
}

