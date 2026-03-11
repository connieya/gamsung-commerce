package com.loopers.domain.payment.event;

public interface PaymentCompletedEventPublisher {

    void publish(PaymentCompletedEvent event);
}
