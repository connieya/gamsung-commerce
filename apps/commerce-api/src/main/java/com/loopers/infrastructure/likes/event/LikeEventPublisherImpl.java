package com.loopers.infrastructure.likes.event;

import com.loopers.domain.likes.event.LikeEventPublisher;
import com.loopers.domain.likes.event.ProductLikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeEventPublisherImpl implements LikeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "like-update-topic-v1";

    @Override
    public void publishEvent(ProductLikeEvent.Update event) {
        ProductLikeEvent.Update update = ProductLikeEvent.Update.of(event.productId(), event.updateType());

        kafkaTemplate.send(TOPIC_NAME, event.productId().toString(), update);

    }
}
