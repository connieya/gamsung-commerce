package com.loopers.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class KafkaMessage<T> {
    private String eventId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Seoul")
    private ZonedDateTime publishedAt;
    private T payload;

    private KafkaMessage(String eventId, ZonedDateTime publishedAt, T payload) {
        this.eventId = eventId;
        this.publishedAt = publishedAt;
        this.payload = payload;
    }

    public static <T> KafkaMessage<T> of(T payload) {
        return new KafkaMessage<>(UUID.randomUUID().toString(), ZonedDateTime.now(), payload);
    }
}
