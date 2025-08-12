package com.loopers.domain.likes;

import java.util.Optional;

public interface LikeSummaryRepository {

    LikeSummary save(LikeSummary likeSummary);

    Optional<LikeSummary> findByTarget(LikeTarget likeTarget);

    Optional<LikeSummary> findByTargetUpdate(LikeTarget likeTarget);
}
