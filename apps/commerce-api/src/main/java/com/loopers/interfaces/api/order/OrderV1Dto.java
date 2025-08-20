package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;
import lombok.Builder;
import lombok.Getter;

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
    }
}
