package com.loopers.domain.activity.event;

public interface ActivityEventPublisher {

    void publishEvent(ActivityEvent.View event);
}
