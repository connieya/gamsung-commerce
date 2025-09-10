package com.loopers.domain.common;

public interface DomainEventPublisher {

    void publishEvent(DomainEvent.Audit event);
}
