package com.loopers.infrastructure.point.jpa;

import com.loopers.infrastructure.point.entity.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointEntity ,Long> {
    Optional<PointEntity> findByUserId(String userId);
}
