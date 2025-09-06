package com.loopers.domain.likes;

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
