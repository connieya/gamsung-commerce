package com.loopers.application.payment.processor;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("CARD")
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void pay(PaymentProcessContext paymentProcessContext) {
        Order order = orderService.getOrder(paymentProcessContext.getOrderId());

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(order.getId(), order.getOrderNumber(), paymentProcessContext.getCardType(), paymentProcessContext.getCardNumber(), order.getFinalAmount(), order.getUserId(), paymentProcessContext.getCouponId());

        paymentService.requestPayment(transaction);

    }
}
