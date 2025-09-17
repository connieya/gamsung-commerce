package com.loopers.infrastructure.payment.event;

import com.loopers.domain.KafkaMessage;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.payment.event.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "payment-topic-v1";

    @Override
    public void publishEvent(PaymentEvent.Success event) {
        KafkaMessage<PaymentEvent.Success> kafkaMessage = KafkaMessage.of(event);
        kafkaTemplate.send(TOPIC_NAME, event.orderId().toString(), kafkaMessage);
    }
}
