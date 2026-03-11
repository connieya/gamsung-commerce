package com.loopers.infrastructure.payment.event;

import com.loopers.domain.KafkaMessage;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentCompletedEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedEventPublisherImpl implements PaymentCompletedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment.completed";

    @Override
    public void publish(PaymentCompletedEvent event) {
        KafkaMessage<PaymentCompletedEvent> message = KafkaMessage.of(event);
        kafkaTemplate.send(TOPIC, event.orderNumber(), message);
        log.info("[결제완료 이벤트 발행] orderNumber={}, userId={}, couponId={}, status={}",
                event.orderNumber(), event.userId(), event.couponId(), event.status());
    }
}
