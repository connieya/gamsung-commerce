package com.loopers.domain.likes;


import java.util.Optional;

public interface LikeSummaryRepository {
    void updateLikeCountBy(Long productId, LikeTargetType likeTarget, Long likeChanged);

    LikeSummary save(LikeSummary likeSummary);

    Optional<LikeSummary> findByTarget(LikeTarget likeTarget);
}
