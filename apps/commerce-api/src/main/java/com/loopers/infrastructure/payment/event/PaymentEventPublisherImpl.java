package com.loopers.infrastructure.payment.event;

import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.payment.event.PaymentEventPublisher;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final OrderApiClient orderApiClient;

    @Override
    public void publishEvent(PaymentEvent.Success event) {
        log.info("[결제성공→주문완료] orderId={}, orderNumber={}", event.orderId(), event.orderNumber());
        orderApiClient.completeOrder(event.orderId());
    }
}
