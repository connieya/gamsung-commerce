package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.LikeTargetType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LikeSummary l " +
            "         set l.likeCount = l.likeCount + :likeChanged " +
            "       where l.target.id   = :productId " +
            "         and l.target.type = :type")
    void updateLikeCountBy(@Param("productId") Long productId,
                          @Param("type") LikeTargetType type,
                          @Param("likeChanged") Long likeChanged);

    Optional<LikeSummary> findByTarget(LikeTarget likeTarget);
}
