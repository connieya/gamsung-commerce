package com.loopers.application.payment.processor;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("POINT")
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final PointService pointService;
    private final OrderService orderService;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    @Transactional
    public void pay(PaymentProcessContext paymentProcessContext) {
        Order order = orderService.getOrder(paymentProcessContext.getOrderId());
        PaymentCommand.Create create = PaymentCommand.Create.of(paymentProcessContext.getOrderId(), paymentProcessContext.getUserId(), PaymentMethod.POINT, order.getFinalAmount());

        pointService.deduct(create.userId(), order.getFinalAmount());

        applicationEventPublisher.publishEvent(PaymentEvent.Success.of(order.getId(), order.getOrderNumber(), paymentProcessContext.getUserId(), PaymentMethod.POINT, order.getFinalAmount(), order.getOrderLines(), paymentProcessContext.getCouponId()));

    }
}
