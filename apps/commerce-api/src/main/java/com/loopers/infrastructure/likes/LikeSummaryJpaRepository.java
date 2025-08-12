package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {
    Optional<LikeSummary> findByTarget(LikeTarget target);
}
