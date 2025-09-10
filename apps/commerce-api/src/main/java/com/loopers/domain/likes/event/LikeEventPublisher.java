package com.loopers.domain.likes.event;

public interface LikeEventPublisher {

    void publishEvent(ProductLikeEvent.Update event);
}
