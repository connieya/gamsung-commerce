package com.loopers.domain.like.event;

public interface LikeEventPublisher {

    void publishEvent(LikeEvent.Update event);
}
