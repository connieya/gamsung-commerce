package com.loopers.infrastructure.point;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointEntity ,Long> {
    Optional<PointEntity> findByUserId(String userId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from PointEntity p where p.userId = :userId")
    Optional<PointEntity> findByUserIdForUpdate(@Param("userId") String userId);
}
