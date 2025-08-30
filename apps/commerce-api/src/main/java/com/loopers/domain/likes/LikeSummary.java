package com.loopers.domain.likes;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.likes.exception.LikeException;
import com.loopers.support.error.ErrorType;
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

    private static final int MIN_LIKE_COUNT = 0;

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

    public void increase() {
        this.likeCount++;
    }

    public void updateCount(Long count) {
        this.likeCount = count;
    }

    public void decrease() {
        if (this.likeCount <= MIN_LIKE_COUNT) {
            throw new LikeException.LikeCountCannotBeNegativeException(ErrorType.LIKE_COUNT_CANNOT_BE_NEGATIVE);
        }
        this.likeCount--;
    }
}
