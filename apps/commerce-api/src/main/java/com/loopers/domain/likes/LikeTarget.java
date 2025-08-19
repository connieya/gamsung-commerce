package com.loopers.domain.likes;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id", "type"})
public class LikeTarget {

    @Column(name = "target_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private LikeTargetType type;

    @Builder
    private LikeTarget(Long id, LikeTargetType type) {
        if (id == null) {
            throw new CoreException(ErrorType.BAD_REQUEST ,"좋아요 대상 ID 는 필수입니다.");
        }

        if (type == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "좋아요 대상 타입은 필수입니다.");
        }
        this.id = id;
        this.type = type;
    }

    public static LikeTarget create(Long id, LikeTargetType type) {
        return LikeTarget.builder()
                .id(id)
                .type(type)
                .build();
    }
}
