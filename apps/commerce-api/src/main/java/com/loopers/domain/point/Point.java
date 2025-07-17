package com.loopers.domain.point;

import com.loopers.domain.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Point extends SelfValidating<Point> {

    @NotBlank
    private String userId;

    @Positive
    private Long value;

    @Builder
    public Point(String userId, Long value) {
        this.userId = userId;
        this.value = value;

        this.validateSelf();
    }

    public static Point create(String userId, Long value) {
        Point point = new Point();

        point.userId = userId;
        point.value = value;
        point.validateSelf();

        return point;
    }

    public void charge(Long value) {
        this.value += value;
    }
}
