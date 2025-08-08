package com.loopers.application.order;

import com.loopers.domain.order.OrderInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class OrderResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final Long orderId;
        private final Long totalAmount;
        private final Long discountAmount;

        public static Create from(OrderInfo orderInfo) {
            return Create.builder()
                    .orderId(orderInfo.getOrderId())
                    .totalAmount(orderInfo.getTotalAmount())
                    .discountAmount(orderInfo.getDiscountAmount())
                    .build();
        }
    }
}
