package com.loopers.interfaces.consumer.likes;

import com.loopers.config.KafkaConfig;
import com.loopers.domain.likes.LikeSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeSummaryConsumer {

    private static final String LIKE_UPDATE_TOPIC = "like-update-topic-v1";
    private static final String LIKE_SUMMARY_GROUP_ID = "like-summary-aggregator-group";

    private final LikeSummaryService likeSummaryService;

    @KafkaListener(
            topics = LIKE_UPDATE_TOPIC,
            groupId = LIKE_SUMMARY_GROUP_ID,
            containerFactory = KafkaConfig.BATCH_LISTENER // KafkaConfig에 정의한 배치 리스너
    )
    public void consumeLikeUpdate(List<ConsumerRecord<String, LikeUpdatedEvent>> records, Acknowledgment ack) {
        log.info("[KAFKA-CONSUME] Group: '{}', Received a batch of {} records.",
                LIKE_SUMMARY_GROUP_ID, records.size());

        try {

            likeSummaryService.update(
                    records.stream()
                            .map(ConsumerRecord::value)
                            .collect(Collectors.toList())

            );
            ack.acknowledge();
            log.info("[KAFKA-CONSUME] Group: '{}', Batch processing successful. Acknowledged {} records.",
                    LIKE_SUMMARY_GROUP_ID, records.size());
        } catch (Exception e) {
            // 서비스 로직 처리 중 어떤 예외라도 발생하면, 커밋하지 않고 재처리를 유도합니다.
            log.error("[KAFKA-CONSUME] Group: '{}', Failed to process batch. Offset will not be committed.",
                    LIKE_SUMMARY_GROUP_ID, e);
        }


    }
}
