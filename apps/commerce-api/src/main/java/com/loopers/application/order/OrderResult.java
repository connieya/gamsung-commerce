package com.loopers.application.order;

import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
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
        private String orderNumber;
        private Long totalAmount;
        private Long discountAmount;
        private OrderStatus orderStatus;
        private ZonedDateTime createdAt;
        private java.util.List<OrderItem> orderItems;


        public static GetDetail from(OrderInfo orderInfo) {
            return GetDetail
                    .builder()
                    .orderId(orderInfo.getOrderId())
                    .orderNumber(orderInfo.getOrderNumber())
                    .totalAmount(orderInfo.getTotalAmount())
                    .discountAmount(orderInfo.getDiscountAmount())
                    .orderStatus(orderInfo.getOrderStatus())
                    .createdAt(orderInfo.getCreatedAt())
                    .orderItems(orderInfo.getOrderItems().stream()
                            .map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getPrice()))
                            .toList())
                    .build();
        }

        public record OrderItem(Long productId, Long quantity, Long price) {}
    }

    @Builder
    @Getter
    public static class List {
        private java.util.List<OrderSummary> orders;

        public static List from(java.util.List<OrderInfo> orderInfos) {
            return List.builder()
                    .orders(orderInfos.stream()
                            .map(OrderSummary::from)
                            .toList())
                    .build();
        }

        @Builder
        @Getter
        public static class OrderSummary {
            private Long orderId;
            private String orderNumber;
            private OrderStatus orderStatus;
            private Long totalAmount;
            private ZonedDateTime createdAt;

            public static OrderSummary from(OrderInfo orderInfo) {
                return OrderSummary.builder()
                        .orderId(orderInfo.getOrderId())
                        .orderNumber(orderInfo.getOrderNumber())
                        .orderStatus(orderInfo.getOrderStatus())
                        .totalAmount(orderInfo.getTotalAmount())
                        .createdAt(orderInfo.getCreatedAt())
                        .build();
            }
        }
    }
}
