package com.loopers.infrastructure.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.Point;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "points")
public class PointEntity extends BaseEntity {

    private String userId;
    private Long value;
    public static PointEntity from(Point point) {
        PointEntity pointEntity = new PointEntity();

        pointEntity.userId = point.getUserId();
        pointEntity.value = point.getValue();

        return pointEntity;
    }

    public Point toDomain() {
        return Point
                .builder()
                .id(id)
                .userId(userId)
                .value(value)
                .build();
    }

    public void changeValue(Long value) {
        this.value = value;
    }
}
