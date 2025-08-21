package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.domain.order.OrderService;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final Map<String, PaymentProcessor> paymentProcessorMap;
    private final PaymentService paymentService;

    @Transactional
    public PaymentResult pay(PaymentCriteria.Pay criteria) {
        Order order = orderService.getOrder(criteria.orderId());
        order.validatePay();

        PaymentProcessor paymentProcessor = paymentProcessorMap.get(criteria.paymentMethod().toString());
        Payment payment = paymentProcessor.pay(PaymentProcessContext.of(criteria));

        return PaymentResult.from(payment);
    }

    @Transactional
    public void complete(PaymentCriteria.Complete complete) {
        PaymentCommand.Search search = PaymentCommand.Search.of(complete.transactionKey() , complete.orderNumber());
        paymentService.complete(search);
    }
}
