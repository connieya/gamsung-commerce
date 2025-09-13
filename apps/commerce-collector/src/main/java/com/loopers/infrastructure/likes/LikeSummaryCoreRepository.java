package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.LikeTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class LikeSummaryCoreRepository implements LikeSummaryRepository {

    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Override
    public void updateLikeCountBy(Long productId, LikeTargetType likeTarget, Long likeChanged) {
        likeSummaryJpaRepository.updateLikeCountBy(productId, likeTarget, likeChanged);
    }

    @Override
    public LikeSummary save(LikeSummary likeSummary) {
        return likeSummaryJpaRepository.save(likeSummary);
    }

    @Override
    public Optional<LikeSummary> findByTarget(LikeTarget likeTarget) {
        return likeSummaryJpaRepository.findByTarget(likeTarget);
    }
}
