package com.loopers.infrastructure.kafka.coupon;

import com.loopers.config.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.coupon.UserCouponService;
import com.loopers.domain.coupon.event.PaymentCompletedEvent;
import com.loopers.domain.coupon.exception.CouponException;
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
            PaymentCompletedEvent event = record.value().getPayload();
            try {
                if (event.couponId() == null || !"SUCCESS".equals(event.status())) {
                    continue;
                }
                userCouponService.use(event.userId(), event.couponId());
            } catch (CouponException.UserCouponNotFoundException | CouponException.UserCouponAlreadyUsedException e) {
                // 비즈니스 예외: 재처리해도 결과가 동일하므로 경고 로그 후 스킵
                log.warn("[쿠폰 사용 처리 스킵] orderNumber={}, couponId={}, reason={}",
                        event.orderNumber(), event.couponId(), e.getMessage());
            } catch (Exception e) {
                // 인프라/예상치 못한 예외: ack 보류하고 재처리
                log.error("[쿠폰 사용 처리 실패 - 재처리 필요] orderNumber={}, couponId={}",
                        event.orderNumber(), event.couponId(), e);
                throw new RuntimeException("쿠폰 사용 처리 중 오류 발생 - 재처리", e);
            }
        }
        ack.acknowledge();
    }
}
