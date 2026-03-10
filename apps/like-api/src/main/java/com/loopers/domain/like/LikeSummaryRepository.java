package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeSummaryRepository {

    LikeSummary save(LikeSummary likeSummary);

    Optional<LikeSummary> findByTarget(LikeTarget likeTarget);

    int increaseLikeCount(LikeTarget target);

    int decreaseLikeCount(LikeTarget target);

    List<LikeSummary> findByTargets(List<LikeTarget> targets);
}
