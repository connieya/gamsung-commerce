package com.loopers.domain.common;

public interface DomainEvent {

    String eventKey();
    String eventName();
    Long userId();

    record Audit(
            String eventKey,
            String eventName,
            Long userId
    ) implements DomainEvent {
        public static Audit from(DomainEvent event) {
            return new Audit(event.eventKey(), event.eventName(), event.userId());
        }
    }
}
