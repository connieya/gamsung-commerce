package com.loopers.domain.payment.event;

public interface PaymentEventPublisher {

    void publishEvent(PaymentEvent.Success event);
}
