package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;
import com.loopers.support.error.ErrorType;
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

        PaymentCommand.Create create = PaymentCommand.Create.of(paymentProcessContext.getOrderId(), paymentProcessContext.getUserId(), PaymentMethod.CARD, order.getFinalAmount());
        Payment payment = paymentService.create(create, PaymentStatus.PENDING);

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(order.getOrderNumber(), payment.getId(), paymentProcessContext.getCardType(), paymentProcessContext.getCardNumber(), order.getFinalAmount());
        Payment requestedPayment = paymentService.requestPayment(transaction);

        return PaymentResult.from(requestedPayment);

    }
}
