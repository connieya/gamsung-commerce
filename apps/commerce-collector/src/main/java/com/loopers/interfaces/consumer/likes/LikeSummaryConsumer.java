package com.loopers.interfaces.consumer.likes;

import com.loopers.config.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.likes.LikeCommand;
import com.loopers.domain.likes.LikeSummaryService;
import com.loopers.domain.likes.LikeUpdateType;
import com.loopers.domain.metrics.MetricCommand;
import com.loopers.domain.metrics.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeSummaryConsumer {

    private static final String LIKE_UPDATE_TOPIC = "like-update-topic-v1";
    private static final String LIKE_SUMMARY_GROUP_ID = "like-summary-aggregator-group";
    private static final String METRIC_GROUP_ID = "metric-aggregator-group";

    private final LikeSummaryService likeSummaryService;
    private final MetricService metricService;

    @KafkaListener(
            topics = LIKE_UPDATE_TOPIC,
            groupId = LIKE_SUMMARY_GROUP_ID,
            containerFactory = KafkaConfig.BATCH_LISTENER // KafkaConfig에 정의한 배치 리스너
    )
    public void consumeLikeUpdate(List<KafkaMessage<LikeUpdatedEvent>> messages, Acknowledgment ack) throws IOException {
        log.info("[KAFKA-CONSUME] Group: '{}', Received a batch of {} messages.",
                LIKE_SUMMARY_GROUP_ID, messages.size());

        List<LikeCommand.Update.Item> items = new ArrayList<>();
        for (KafkaMessage<LikeUpdatedEvent> likeUpdatedEvent : messages) {
            LikeUpdateType likeUpdateType = likeUpdatedEvent.getPayload().updateType();
            Long productId = likeUpdatedEvent.getPayload().productId();
            String eventId = likeUpdatedEvent.getEventId();
            items.add(LikeCommand.Update.Item.of(eventId, productId, likeUpdateType));
        }

        likeSummaryService.update(new LikeCommand.Update(items));
        ack.acknowledge();
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
