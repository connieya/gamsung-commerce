package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeTarget;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {
    Optional<LikeSummary> findByTarget(LikeTarget target);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LikeSummary l where l.target = :likeTarget")
    Optional<LikeSummary> findByTargetForUpdate(LikeTarget likeTarget);
}
