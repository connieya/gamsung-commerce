package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "target_id", "target_type"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Embedded
    private LikeTarget target;

    @Builder
    private Like(Long userId, LikeTarget target) {
        this.userId = userId;
        this.target = target;
    }

    public static Like create(Long userId, Long targetId, LikeTargetType targetType) {
        return Like.builder()
                .userId(userId)
                .target(LikeTarget.create(targetId, targetType))
                .build();
    }
}
