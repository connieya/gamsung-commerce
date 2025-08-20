package com.loopers.application.order;

import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    @Builder
    @Getter
    public static class GetDetail {
        private Long orderId;
        private Long totalAmount;
        private Long discountAmount;
        private OrderStatus orderStatus;
        private List<OrderItem> orderItems;


        public static GetDetail from(OrderInfo orderInfo) {
            return GetDetail
                    .builder()
                    .orderId(orderInfo.getOrderId())
                    .totalAmount(orderInfo.getTotalAmount())
                    .discountAmount(orderInfo.getDiscountAmount())
                    .build();
        }

        public static class OrderItem {
            private Long orderProductId;
            private Long quantity;
            private Long productId;
        }

    }
}
