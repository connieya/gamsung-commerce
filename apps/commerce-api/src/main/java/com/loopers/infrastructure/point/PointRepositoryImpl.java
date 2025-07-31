package com.loopers.infrastructure.point;


import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;


    @Override
    public Optional<Point> findByUserId(String userId) {
        return pointJpaRepository.findByUserId(userId).map(PointEntity::toDomain);
    }
}
