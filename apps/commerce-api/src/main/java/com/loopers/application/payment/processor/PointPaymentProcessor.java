package com.loopers.application.payment.processor;

import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("POINT")
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final PointService pointService;
    private final OrderApiClient orderApiClient;

    @Override
    @Transactional
    public void pay(PaymentProcessContext paymentProcessContext) {
        OrderApiDto.OrderResponse order = orderApiClient.getOrder(paymentProcessContext.getOrderId()).data();

        pointService.deduct(paymentProcessContext.getUserId(), order.finalAmount());

        orderApiClient.completeOrder(order.orderId());
    }
}
