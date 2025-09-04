package com.loopers.domain.likes.event;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.likes.exception.LikeException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;


@Component
@RequiredArgsConstructor
@Slf4j
public class LikeSummaryEventListener {

    private final LikeSummaryRepository likeSummaryRepository;
    private final ProductLikeRepository productLikeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_NAME = "like-update-topic-v1";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void add(ProductLikeEvent.Add event) {
        ProductLikeEvent.Update update = new ProductLikeEvent.Update(event.productId(), ProductLikeEvent.Update.UpdateType.INCREMENT);
        try {
            // kafkaTemplate.send(토픽이름, 메시지Key, 메시지Value)
            // 메시지 Key(event.productId().toString())를 사용하면, 동일한 productId를 가진 메시지는
            // 항상 같은 파티션으로 전송되는 것을 보장할 수 있습니다. (메시지 순서 보장에 유리)
            kafkaTemplate.send(TOPIC_NAME, event.productId().toString(), update);
            log.info("Successfully sent message to Kafka topic: {}", TOPIC_NAME);
        } catch (Exception e) {
            // 카프카 전송 실패 시 로그 (운영에서는 에러 모니터링 또는 별도 처리 필요)
            log.error("Failed to send message to Kafka", e);
        }

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void remove(ProductLikeEvent.Remove event) {
        LikeSummary likeSummary = likeSummaryRepository.findByTargetUpdate(LikeTarget.create(event.productId(), event.likeTargetType()))
                .orElseThrow(() -> new LikeException.LikeSummaryNotFoundException(ErrorType.LIKE_SUMMARY_NOT_FOUND));
        likeSummary.decrease();
    }
}
