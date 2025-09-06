package com.loopers.interfaces.consumer.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityConsumer {

    private static final String ACTIVITY_PRODUCT_VIEW_TOPIC = "product-view-topic-v1";
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = ACTIVITY_PRODUCT_VIEW_TOPIC,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void consumeActivityView(List<ConsumerRecord<String ,String>> records, Acknowledgment ack) {
        log.info("Activity view consume : {}" ,records.size());

        List<ActivityViewEvent> activityViewEvents = records.stream()
                .map(ConsumerRecord::value)
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, ActivityViewEvent.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

}
