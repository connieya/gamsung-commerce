package com.loopers.infrastructure;

import com.loopers.domain.likes.LikeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long> {
    @Modifying
    @Query("update LikeSummary l set l.likeCount = l.likeCount + :likeChanged where l.target.id = :productId")
    void updateLikeCountBy(Long productId, Long likeChanged);
}
