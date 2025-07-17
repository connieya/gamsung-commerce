package com.loopers.application.point.port.out;

import com.loopers.domain.point.Point;

import java.util.Optional;

public interface PointRepositoryOut {

    Optional<Point> findByUserId(String userId);
}
