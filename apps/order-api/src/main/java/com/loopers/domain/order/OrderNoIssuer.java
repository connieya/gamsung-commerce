package com.loopers.domain.order;

public interface OrderNoIssuer {
    OrderNoIssue issue(boolean isNewOrderForm);

    void verify(String orderNo, String orderSignature, String orderKey);
}

