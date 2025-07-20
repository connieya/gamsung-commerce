package com.loopers.domain.point;

import com.loopers.application.point.exception.PointException;
import com.loopers.domain.common.Validatable;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Point extends Validatable<Point> {

    @NotBlank
    private String userId;

    @PositiveOrZero
    private Long value;

    @Builder
    public Point(String userId, Long value) {
        this.userId = userId;
        this.value = value;

        this.validate();
    }

    public static Point create(String userId, Long value) {
        Point point = new Point();

        point.userId = userId;
        point.value = value;
        point.validate();

        return point;
    }

    public void charge(Long value) {
        if (value <= 0) {
            throw new PointException.PointInvalidChargeAmountException(ErrorType.POINT_INVALID_CHARGE_AMOUNT);
        }
        this.value += value;
    }
}
