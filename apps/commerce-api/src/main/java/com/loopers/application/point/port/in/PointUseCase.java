package com.loopers.application.point.port.in;

public interface PointUseCase {

    PointInfoResult getPoint(String userId);

    PointInfoResult charge(String userId, Long value);
}
