package com.loopers.domain.likes.event;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.likes.exception.LikeException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class LikeSummaryEventListener {

    private final LikeSummaryRepository likeSummaryRepository;
    private final ProductLikeRepository productLikeRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void add(ProductLikeEvent.Add event) {
        Long likeCount = productLikeRepository.getLikeCount(event.productId());

        likeSummaryRepository.findByTargetUpdate(
                LikeTarget.create(event.productId(), event.likeTargetType())
        ).ifPresentOrElse(
                likeSummary -> likeSummary.updateCount(likeCount),
                () -> {
                    LikeSummary likeSummary = LikeSummary.create(event.productId(), event.likeTargetType());
                    likeSummary.updateCount(likeCount);
                    likeSummaryRepository.save(likeSummary);
                }
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void remove(ProductLikeEvent.Remove event) {
        LikeSummary likeSummary = likeSummaryRepository.findByTargetUpdate(LikeTarget.create(event.productId(), event.likeTargetType()))
                .orElseThrow(() -> new LikeException.LikeSummaryNotFoundException(ErrorType.LIKE_SUMMARY_NOT_FOUND));
        likeSummary.decrease();
    }
}
