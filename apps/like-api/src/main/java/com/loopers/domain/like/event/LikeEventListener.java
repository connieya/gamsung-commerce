package com.loopers.domain.like.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeEventListener {

    private final LikeEventPublisher likeEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onAdd(LikeEvent.Add event) {
        LikeEvent.Update update = LikeEvent.Update.of(
                event.targetId(), event.targetType(), LikeEvent.Update.UpdateType.INCREMENT);
        likeEventPublisher.publishEvent(update);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onRemove(LikeEvent.Remove event) {
        LikeEvent.Update update = LikeEvent.Update.of(
                event.targetId(), event.targetType(), LikeEvent.Update.UpdateType.DECREMENT);
        likeEventPublisher.publishEvent(update);
    }
}
