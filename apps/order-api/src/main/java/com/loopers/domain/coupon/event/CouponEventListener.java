package com.loopers.domain.coupon.event;

import com.loopers.config.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.coupon.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private static final String TOPIC = "payment.completed";
    private static final String GROUP_ID = "coupon-consumer-group";

    private final UserCouponService userCouponService;

    @KafkaListener(
            topics = TOPIC,
            groupId = GROUP_ID,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handle(List<ConsumerRecord<String, KafkaMessage<PaymentCompletedEvent>>> messages, Acknowledgment ack) {
        for (ConsumerRecord<String, KafkaMessage<PaymentCompletedEvent>> record : messages) {
            try {
                PaymentCompletedEvent event = record.value().getPayload();
                if (event.couponId() == null) {
                    continue;
                }
                if (!"SUCCESS".equals(event.status())) {
                    continue;
                }
                userCouponService.use(event.userId(), event.couponId());
            } catch (Exception e) {
                log.error("[쿠폰 사용 처리 실패] orderNumber={}, error={}", record.value().getPayload().orderNumber(), e.getMessage(), e);
            }
        }
        ack.acknowledge();
    }
}
