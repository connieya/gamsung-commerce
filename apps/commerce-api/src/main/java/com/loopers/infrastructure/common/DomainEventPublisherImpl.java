package com.loopers.infrastructure.common;

import com.loopers.domain.common.DomainEvent;
import com.loopers.domain.common.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisherImpl implements DomainEventPublisher {

    private static final String TOPIC_NAME = "common-audit-v1";
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public void publishEvent(DomainEvent.Audit event) {
        kafkaTemplate.send(TOPIC_NAME, event);
    }
}
