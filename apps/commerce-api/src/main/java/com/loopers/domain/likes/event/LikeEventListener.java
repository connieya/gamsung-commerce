package com.loopers.domain.likes.event;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.likes.exception.LikeException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;


@Component
@RequiredArgsConstructor
@Slf4j
public class LikeEventListener {

    private final LikeEventPublisher likeEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void add(ProductLikeEvent.Add event) {
        ProductLikeEvent.Update update = ProductLikeEvent.Update.of(event.productId(), ProductLikeEvent.Update.UpdateType.INCREMENT);
        likeEventPublisher.publishEvent(update);

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void remove(ProductLikeEvent.Remove event) {
        ProductLikeEvent.Update update = ProductLikeEvent.Update.of(event.productId(), ProductLikeEvent.Update.UpdateType.DECREMENT);
        likeEventPublisher.publishEvent(update);
    }
}
