package com.loopers.interfaces.consumer.payment;

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
public class PaymentConsumer {

    private static final String TOPIC_NAME = "payment-topic-v1";
    private final MetricService metricService;

    @KafkaListener(
            topics = TOPIC_NAME,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void consumePayment(List<KafkaMessage<PaymentEvent>> messages , Acknowledgment ack) {
        log.info("consume payment =  {} " , messages.size());

        for (KafkaMessage<PaymentEvent> message : messages) {
            List<MetricCommand.Aggregate.Item> commands = new ArrayList<>();
            String eventId = message.getEventId();
            LocalDate date = message.getPublishedAt().toLocalDate();
            PaymentEvent payload = message.getPayload();
            List<PaymentEvent.Item> items = payload.orderLines();
            for (PaymentEvent.Item item : items) {
                Long productId = item.productId();
                commands.add(MetricCommand.Aggregate.Item.ofSaleCount(eventId,date,productId,1L));
            }
            metricService.aggregate(new MetricCommand.Aggregate(commands));
        }

        ack.acknowledge();
    }
}
