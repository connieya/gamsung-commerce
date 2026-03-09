package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import com.loopers.domain.like.LikeTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class LikeSummaryCoreRepository implements LikeSummaryRepository {

    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Override
    public LikeSummary save(LikeSummary likeSummary) {
        return likeSummaryJpaRepository.save(likeSummary);
    }

    @Override
    public Optional<LikeSummary> findByTarget(LikeTarget likeTarget) {
        return likeSummaryJpaRepository.findByTarget(likeTarget);
    }

    @Override
    public Optional<LikeSummary> findByTargetForUpdate(LikeTarget likeTarget) {
        return likeSummaryJpaRepository.findByTargetForUpdate(likeTarget);
    }

    @Override
    public List<LikeSummary> findByTargets(List<LikeTarget> targets) {
        return likeSummaryJpaRepository.findByTargetIn(targets);
    }
}
