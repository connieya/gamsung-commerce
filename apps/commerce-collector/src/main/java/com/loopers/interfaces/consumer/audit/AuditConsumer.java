package com.loopers.interfaces.consumer.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditConsumer {

    private static final String AUDIT_TOPIC = "common-audit-v1";

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = AUDIT_TOPIC,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onDomainAudited(List<ConsumerRecord<String, String>> messages , Acknowledgment ack) {
        String topic = messages.getFirst().topic();
    }


}
