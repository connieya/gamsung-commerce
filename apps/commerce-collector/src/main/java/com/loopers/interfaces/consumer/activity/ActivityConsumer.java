package com.loopers.interfaces.consumer.activity;

import com.loopers.config.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.metrics.MetricCommand;
import com.loopers.domain.metrics.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MetricService metricService;

    @KafkaListener(
            topics = ACTIVITY_PRODUCT_VIEW_TOPIC,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void consumeActivityView(List<KafkaMessage<ActivityViewEvent>> messages, Acknowledgment ack) {
        log.info("Activity view consume : {}" ,messages.size());
        List<MetricCommand.Aggregate.Item> items = new ArrayList<>();
        for (KafkaMessage<ActivityViewEvent> message : messages) {
            Long productId = message.getPayload().productId();
            String eventId = message.getEventId();
            LocalDate date = message.getPublishedAt().toLocalDate();
            items.add(MetricCommand.Aggregate.Item.ofViewCount(eventId,date,productId,1L));
        }
        metricService.aggregate(new MetricCommand.Aggregate(items));
        ack.acknowledge();


    }

}
