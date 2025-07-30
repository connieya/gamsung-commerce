package com.loopers.domain.point;

public interface PointUseCase {

    PointInfoResult getPoint(String userId);

    PointInfoResult charge(String userId, Long value);
}
