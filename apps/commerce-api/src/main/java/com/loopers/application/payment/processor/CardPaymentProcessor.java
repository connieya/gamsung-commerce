package com.loopers.application.payment.processor;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;
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

        PaymentCommand.Create create = PaymentCommand.Create.of(paymentProcessContext.getOrderId(), paymentProcessContext.getUserId(), PaymentMethod.CARD, order.getFinalAmount());
        Payment payment = paymentService.create(create, PaymentStatus.PENDING);

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(order.getOrderNumber(), payment.getId(), paymentProcessContext.getCardType(), paymentProcessContext.getCardNumber(), order.getFinalAmount());
        try{
            PgSimulatorResponse.RequestTransaction requestTransaction = paymentAdapter.request(transaction);
            paymentService.execute(PaymentCommand.Execute.of(requestTransaction.transactionKey(),requestTransaction.status() ,payment.getId()));
        }catch (Exception e){
            paymentService.fail(payment.getId());
            throw e;
        }

        return payment;
    }
}
