package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {

    Optional<LikeSummary> findByTarget(LikeTarget target);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO like_summary (target_id, target_type, like_count, created_at, updated_at) " +
                   "VALUES (:targetId, :targetType, 1, NOW(), NOW()) " +
                   "ON DUPLICATE KEY UPDATE like_count = like_count + 1, updated_at = NOW()",
           nativeQuery = true)
    int increaseLikeCount(@Param("targetId") Long targetId,
                          @Param("targetType") String targetType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LikeSummary l set l.likeCount = l.likeCount - 1 where l.target = :target and l.likeCount > 0")
    int decreaseLikeCount(@Param("target") LikeTarget target);

    @Query("select l from LikeSummary l where l.target in :targets")
    List<LikeSummary> findByTargetIn(@Param("targets") List<LikeTarget> targets);
}
