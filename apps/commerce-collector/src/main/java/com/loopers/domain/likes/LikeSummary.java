package com.loopers.domain.likes;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "like_summary",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"target_id", "target_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LikeSummary extends BaseEntity {

    @Column(name = "like_count")
    private Long likeCount;

    @Embedded
    private LikeTarget target;


    @Builder
    private LikeSummary(Long likeCount, LikeTarget target) {
        this.likeCount = likeCount;
        this.target = target;
    }

    public static LikeSummary create(Long targetId, LikeTargetType targetType) {
        return LikeSummary.builder()
                .likeCount(0L)
                .target(LikeTarget.create(targetId, targetType))
                .build();
    }

}
