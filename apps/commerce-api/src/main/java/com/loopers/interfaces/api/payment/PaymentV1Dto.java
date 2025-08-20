package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;

public class PaymentV1Dto {

    public static class Request {
        public record Pay(Long orderId, PaymentMethod paymentMethod) {


        }
    }

    public static class Response {
        public record Pay(
                Long paymentId,
                PaymentStatus paymentStatus) {
            public static Pay from(PaymentResult paymentResult) {
                return new Pay(paymentResult.getPaymentId(),paymentResult.getPaymentStatus());
            }
        }
    }
}
