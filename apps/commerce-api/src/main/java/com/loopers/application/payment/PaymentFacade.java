package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.domain.order.OrderService;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.CardType;
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

    public void pay(PaymentCriteria.Pay criteria) {
        Order order = orderService.getOrder(criteria.orderId());
        order.validatePay();

        paymentService.ready(PaymentCommand.Ready.of(order.getId(), order.getOrderNumber(), order.getUserId(), order.getFinalAmount(), criteria.paymentMethod()), null);

        PaymentProcessor paymentProcessor = paymentProcessorMap.get(criteria.paymentMethod().toString());
        paymentProcessor.pay(PaymentProcessContext.of(criteria));
    }
    
    @Transactional
    public PaymentService.PaymentReadyResult ready(String orderNo, String orderKey, PaymentCriteria.Ready criteria) {
        Order order = orderService.getOrderByOrderNumber(orderNo);
        order.validatePay();
        
        PaymentCommand.Ready readyCommand = PaymentCommand.Ready.of(
                order.getId(), 
                order.getOrderNumber(), 
                order.getUserId(), 
                order.getFinalAmount(), 
                criteria.paymentMethod()
        );
        
        return paymentService.ready(readyCommand, orderKey);
    }
    
    @Transactional
    public PaymentService.PaymentSessionResult createPaymentSession(String orderNo, String orderKey, PaymentCriteria.PaymentSession criteria) {
        Order order = orderService.getOrderByOrderNumber(orderNo);
        order.validatePay();
        
        PaymentCommand.Transaction transactionCommand = PaymentCommand.Transaction.of(
                order.getId(),
                order.getOrderNumber(),
                criteria.cardType(),
                criteria.cardNumber(),
                order.getFinalAmount(),
                order.getUserId(),
                criteria.couponId()
        );
        
        return paymentService.createPaymentSession(transactionCommand, orderKey);
    }

    @Transactional
    public void complete(PaymentCriteria.Complete complete) {
        PaymentCommand.Search search = PaymentCommand.Search.of(complete.transactionKey(), complete.orderNumber());
        paymentService.complete(search);
    }
}
