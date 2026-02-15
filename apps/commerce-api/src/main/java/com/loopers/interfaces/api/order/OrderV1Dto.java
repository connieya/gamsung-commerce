package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderV1Dto {

    public static class Request {
        public record Place(Long couponId , List<OrderItem> orderItems) {

        }
    }


    @Builder
    @Getter
    public static class OrderItem {
        private Long productId;
        private Long quantity;
    }


    public static class Response {
        public record Place(Long orderId ,Long totalAmount , Long discountAmount) {
            public static Place from(OrderResult.Create create) {
                return new Place(create.getOrderId(), create.getTotalAmount(), create.getDiscountAmount());
            }
        }

        public record List(java.util.List<OrderSummary> orders) {
            public static List from(OrderResult.List result) {
                return new List(result.getOrders().stream()
                        .map(OrderSummary::from)
                        .toList());
            }

            public record OrderSummary(
                    Long orderId,
                    String orderNumber,
                    OrderStatus orderStatus,
                    Long totalAmount,
                    ZonedDateTime createdAt
            ) {
                public static OrderSummary from(OrderResult.List.OrderSummary summary) {
                    return new OrderSummary(
                            summary.getOrderId(),
                            summary.getOrderNumber(),
                            summary.getOrderStatus(),
                            summary.getTotalAmount(),
                            summary.getCreatedAt()
                    );
                }
            }
        }

        public record Detail(
                Long orderId,
                String orderNumber,
                Long totalAmount,
                Long discountAmount,
                OrderStatus orderStatus,
                ZonedDateTime createdAt,
                java.util.List<OrderLineItem> items
        ) {
            public static Detail from(OrderResult.GetDetail detail) {
                return new Detail(
                        detail.getOrderId(),
                        detail.getOrderNumber(),
                        detail.getTotalAmount(),
                        detail.getDiscountAmount(),
                        detail.getOrderStatus(),
                        detail.getCreatedAt(),
                        detail.getOrderItems().stream()
                                .map(item -> new OrderLineItem(item.productId(), item.quantity(), item.price()))
                                .toList()
                );
            }

            public record OrderLineItem(Long productId, Long quantity, Long price) {}
        }
    }
}
