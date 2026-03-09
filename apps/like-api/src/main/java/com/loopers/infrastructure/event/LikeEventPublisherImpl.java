package com.loopers.infrastructure.event;

import com.loopers.domain.KafkaMessage;
import com.loopers.domain.like.event.LikeEvent;
import com.loopers.domain.like.event.LikeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeEventPublisherImpl implements LikeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "like-update-topic-v1";

    @Override
    public void publishEvent(LikeEvent.Update event) {
        KafkaMessage<LikeEvent.Update> kafkaMessage = KafkaMessage.of(event);
        kafkaTemplate.send(TOPIC_NAME, event.targetId().toString(), kafkaMessage);
    }
}
