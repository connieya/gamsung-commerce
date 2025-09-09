package com.loopers.interfaces.consumer.likes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.likes.LikeCommand;
import com.loopers.domain.likes.LikeSummaryService;
import com.loopers.domain.likes.LikeUpdateType;
import com.loopers.domain.metrics.MetricCommand;
import com.loopers.domain.metrics.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeSummaryConsumer {

    private static final String LIKE_UPDATE_TOPIC = "like-update-topic-v1";
    private static final String LIKE_SUMMARY_GROUP_ID = "like-summary-aggregator-group";
    private static final String METRIC_GROUP_ID = "metric-aggregator-group";

    private final LikeSummaryService likeSummaryService;
    private final ObjectMapper objectMapper;
    private final MetricService metricService;

    @KafkaListener(
            topics = LIKE_UPDATE_TOPIC,
            groupId = LIKE_SUMMARY_GROUP_ID,
            containerFactory = KafkaConfig.BATCH_LISTENER // KafkaConfig에 정의한 배치 리스너
    )
    public void consumeLikeUpdate(List<ConsumerRecord<String, String>> records, Acknowledgment ack) throws IOException {
        log.info("[KAFKA-CONSUME] Group: '{}', Received a batch of {} records.",
                LIKE_SUMMARY_GROUP_ID, records.size());

        List<KafkaMessage<LikeUpdatedEvent>> likeUpdatedEvents = records.stream()
                .map(ConsumerRecord::value)                       // String
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, new TypeReference<KafkaMessage<LikeUpdatedEvent>>() {

                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                })
                .toList();


        List<LikeCommand.Update.Item> items = new ArrayList<>();
        for (KafkaMessage<LikeUpdatedEvent> likeUpdatedEvent : likeUpdatedEvents) {
            LikeUpdateType likeUpdateType = likeUpdatedEvent.getPayload().updateType();
            Long productId = likeUpdatedEvent.getPayload().productId();
            String eventId = likeUpdatedEvent.getEventId();
            items.add(LikeCommand.Update.Item.of(eventId, productId, likeUpdateType));
        }

        try {
            likeSummaryService.update(new LikeCommand.Update(items));
            ack.acknowledge();
            log.info("[KAFKA-CONSUME] Group: '{}', Batch processing successful. Acknowledged {} records.",
                    LIKE_SUMMARY_GROUP_ID, records.size());
        } catch (Exception e) {
            System.out.println("exception = " + e);
            // 서비스 로직 처리 중 어떤 예외라도 발생하면, 커밋하지 않고 재처리를 유도합니다.
            log.error("[KAFKA-CONSUME] Group: '{}', Failed to process batch. Offset will not be committed.",
                    LIKE_SUMMARY_GROUP_ID, e);
        }
    }

    @KafkaListener(
            topics = LIKE_UPDATE_TOPIC,
            groupId = METRIC_GROUP_ID,
            containerFactory = KafkaConfig.BATCH_LISTENER // KafkaConfig에 정의한 배치 리스너
    )
    public void onMetrics(List<KafkaMessage<LikeUpdatedEvent>> messages, Acknowledgment ack) {
        log.info("[kafka consume !!] Group: '{}', Received a batch of {} messages.",
                METRIC_GROUP_ID, messages.size());

        List<MetricCommand.Aggregate.Item> items = new ArrayList<>();

        for (KafkaMessage<LikeUpdatedEvent> message : messages) {
            String eventId = message.getEventId();
            LocalDate date = message.getPublishedAt().toLocalDate();
            Long productId = message.getPayload().productId();
            LikeUpdateType likeUpdateType = message.getPayload().updateType();
            items.add(MetricCommand.Aggregate.Item.ofLikeCount(eventId, date, productId ,likeUpdateType == LikeUpdateType.INCREMENT ? 1L : -1L));
        }
        metricService.aggregate(new MetricCommand.Aggregate(items));
        ack.acknowledge();

    }
}
