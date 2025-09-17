package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeTarget;
import com.loopers.domain.likes.LikeTargetType;
import feign.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {
    Optional<LikeSummary> findByTarget(LikeTarget target);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LikeSummary l where l.target = :likeTarget")
    Optional<LikeSummary> findByTargetForUpdate(LikeTarget likeTarget);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LikeSummary l " +
            "         set l.likeCount = l.likeCount + :likeChanged\n" +
            "       where l.target.id   = :productId\n" +
            "         and l.target.type = :type")
    int updateLikeCountBy(@Param("productId") Long productId,
                          @Param("type") LikeTargetType type,
                          @Param("likeChanged") Long likeChanged);
}
