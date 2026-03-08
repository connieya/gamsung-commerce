package com.loopers.application.payment.processor;

import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.domain.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("CARD")
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;
    private final OrderApiClient orderApiClient;

    @Override
    public void pay(PaymentProcessContext paymentProcessContext) {
        OrderApiDto.OrderResponse order = orderApiClient.getOrder(paymentProcessContext.getOrderId()).data();

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(
                order.orderId(),
                order.orderNumber(),
                PaymentMethod.CARD,
                PayKind.CARD,
                paymentProcessContext.getCardType(),
                paymentProcessContext.getCardNumber(),
                order.finalAmount(),
                order.userId(),
                paymentProcessContext.getCouponId()
        );

        paymentService.requestPayment(transaction);
    }
}
