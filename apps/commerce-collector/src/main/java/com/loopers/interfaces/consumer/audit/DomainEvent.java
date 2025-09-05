package com.loopers.interfaces.consumer.audit;

public record DomainEvent() {
    public record Audit(
            String eventKey,
            String eventName,
            Long userId
    ) {
    }
}
