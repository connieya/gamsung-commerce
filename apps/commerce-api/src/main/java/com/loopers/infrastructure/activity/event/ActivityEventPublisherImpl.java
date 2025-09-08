package com.loopers.infrastructure.activity.event;

import com.loopers.domain.KafkaMessage;
import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.activity.event.ActivityEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityEventPublisherImpl implements ActivityEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "product-view-topic-v1";

    @Override
    public void publishEvent(ActivityEvent.View event) {
        KafkaMessage<ActivityEvent.View> kafkaMessage = KafkaMessage.of(event);
        kafkaTemplate.send(TOPIC_NAME, event.getProductId().toString(), kafkaMessage);
    }
}
