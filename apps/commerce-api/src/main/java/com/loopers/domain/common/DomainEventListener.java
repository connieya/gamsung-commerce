package com.loopers.domain.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final DomainEventPublisher domainEventPublisher;

    @Async
    @EventListener
    public void audit(DomainEvent event) {
        DomainEvent.Audit audit = DomainEvent.Audit.from(event);
        domainEventPublisher.publishEvent(audit);
    }
}
