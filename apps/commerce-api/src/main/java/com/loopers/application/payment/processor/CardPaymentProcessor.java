package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("CARD")
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @Override
    public PaymentResult pay(PaymentProcessContext paymentProcessContext) {
        Order order = orderService.getOrder(paymentProcessContext.getOrderId());

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(order.getId() ,order.getOrderNumber(), paymentProcessContext.getCardType(), paymentProcessContext.getCardNumber(), order.getFinalAmount(), order.getUserId());
        Payment requestedPayment = paymentService.requestPayment(transaction);

        return PaymentResult.from(requestedPayment);

    }
}
