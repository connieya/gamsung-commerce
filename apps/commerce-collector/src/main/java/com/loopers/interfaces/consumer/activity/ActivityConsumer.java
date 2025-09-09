package com.loopers.interfaces.consumer.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.metrics.MetricCommand;
import com.loopers.domain.metrics.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityConsumer {

    private static final String ACTIVITY_PRODUCT_VIEW_TOPIC = "product-view-topic-v1";
    private final ObjectMapper objectMapper;
    private final MetricService metricService;

    @KafkaListener(
            topics = ACTIVITY_PRODUCT_VIEW_TOPIC,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void consumeActivityView(List<ConsumerRecord<String ,String>> records, Acknowledgment ack) {
        log.info("Activity view consume : {}" ,records.size());

        List<KafkaMessage<ActivityViewEvent>> activityViewEvents = records.stream()
                .map(ConsumerRecord::value)
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, new TypeReference<KafkaMessage<ActivityViewEvent>>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        List<MetricCommand.Aggregate.Item> items = new ArrayList<>();
        for (KafkaMessage<ActivityViewEvent> activityViewEvent : activityViewEvents) {
            Long productId = activityViewEvent.getPayload().productId();
            String eventId = activityViewEvent.getEventId();
            LocalDate date = activityViewEvent.getPublishedAt().toLocalDate();
            items.add(MetricCommand.Aggregate.Item.ofViewCount(eventId,date,productId,1L));
        }

        metricService.aggregate(new MetricCommand.Aggregate(items));


    }

}
