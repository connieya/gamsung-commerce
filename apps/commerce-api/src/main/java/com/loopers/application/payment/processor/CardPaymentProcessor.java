package com.loopers.application.payment.processor;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("CARD")
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentAdapter paymentAdapter;
    private final PaymentService paymentService;
    private final OrderService orderService;

    @Override
    public Payment pay(PaymentProcessContext paymentProcessContext) {
        Order order = orderService.getOrder(paymentProcessContext.getOrderId());

        PaymentCommand.Create create = PaymentCommand.Create.of(paymentProcessContext.getOrderId(), paymentProcessContext.getUserId(), PaymentMethod.POINT, order.getFinalAmount());
        Payment payment = paymentService.create(create, PaymentStatus.PENDING);

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(order.getOrderNumber(), paymentProcessContext.getCardType(), paymentProcessContext.getCardNumber(), order.getFinalAmount());
        paymentAdapter.request(transaction);

        return payment;
    }
}
