package com.loopers.infrastructure;

import com.loopers.domain.likes.LikeSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LikeSummaryCoreRepository implements LikeSummaryRepository {

    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Override
    public void updateLikeCountBy(Long productId, Long likeChanged) {
        likeSummaryJpaRepository.updateLikeCountBy(productId,likeChanged);
    }
}
