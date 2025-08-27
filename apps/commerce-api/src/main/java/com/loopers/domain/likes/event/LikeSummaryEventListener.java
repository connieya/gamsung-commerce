package com.loopers.domain.likes.event;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.exception.LikeException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeSummaryEventListener {

    private final LikeSummaryRepository likeSummaryRepository;

    @EventListener
    public void add(ProductLikeEvent.Add event) {
        likeSummaryRepository.findByTargetUpdate(LikeTarget.create(event.productId(), event.likeTargetType()))
                .ifPresentOrElse(
                        LikeSummary::increase,
                        () -> likeSummaryRepository.save(LikeSummary.create(event.productId(), event.likeTargetType()))
                );
    }

    @EventListener
    public void remove(ProductLikeEvent.Remove event) {
        LikeSummary likeSummary = likeSummaryRepository.findByTargetUpdate(LikeTarget.create(event.productId(), event.likeTargetType()))
                .orElseThrow(() -> new LikeException.LikeSummaryNotFoundException(ErrorType.LIKE_SUMMARY_NOT_FOUND));
        likeSummary.decrease();
    }
}
