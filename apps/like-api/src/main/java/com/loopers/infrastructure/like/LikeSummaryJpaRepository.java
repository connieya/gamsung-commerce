package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeTarget;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {

    Optional<LikeSummary> findByTarget(LikeTarget target);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LikeSummary l where l.target = :likeTarget")
    Optional<LikeSummary> findByTargetForUpdate(@Param("likeTarget") LikeTarget likeTarget);

    @Query("select l from LikeSummary l where l.target in :targets")
    List<LikeSummary> findByTargetIn(@Param("targets") List<LikeTarget> targets);
}
