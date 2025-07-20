package com.loopers.infrastructure.point.adapter;


import com.loopers.application.point.port.out.PointRepositoryOut;
import com.loopers.domain.point.Point;
import com.loopers.infrastructure.point.entity.PointEntity;
import com.loopers.infrastructure.point.jpa.PointJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointRepositoryAdapter implements PointRepositoryOut {

    private final PointJpaRepository pointJpaRepository;


    @Override
    public Optional<Point> findByUserId(String userId) {
        return pointJpaRepository.findByUserId(userId).map(PointEntity::toDomain);
    }
}
