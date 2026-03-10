package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeTarget;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Gap Lock Deadlock 재현 테스트 전용 JPA Repository.
 * SELECT ... FOR UPDATE (PESSIMISTIC_WRITE) 를 사용하는 원래 문제 패턴을 재현한다.
 */
public interface LikeSummaryGapLockJpaRepository extends JpaRepository<LikeSummary, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LikeSummary l WHERE l.target = :target")
    Optional<LikeSummary> findByTargetForUpdate(@Param("target") LikeTarget target);

    @Query("SELECT l FROM LikeSummary l WHERE l.target = :target")
    Optional<LikeSummary> findByTarget(@Param("target") LikeTarget target);
}
