package com.loopers.domain.likes;

import com.loopers.interfaces.consumer.likes.LikeUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeSummaryService {

    private final LikeSummaryRepository likeSummaryRepository;


    @Transactional
    public void update(List<LikeUpdatedEvent> events) {
        Map<Long, Long> countChanges = events.stream()
                .collect(Collectors.groupingBy(
                        LikeUpdatedEvent::productId,
                        Collectors.summingLong(event -> event.updateType() == LikeUpdatedEvent.UpdateType.INCREMENT ? 1L : -1L)
                ));

        for (Long productId : countChanges.keySet()) {
            Long likeChanged = countChanges.get(productId);

            likeSummaryRepository.updateLikeCountBy(productId,likeChanged);

        }
    }
}
