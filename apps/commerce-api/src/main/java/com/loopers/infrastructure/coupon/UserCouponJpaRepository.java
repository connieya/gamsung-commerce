package com.loopers.infrastructure.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity , Long> {
    Optional<UserCouponEntity> findByCouponId(Long couponId);

    Optional<UserCouponEntity> findByUserId(Long userId);

    @Modifying
    @Query("update UserCouponEntity uc set uc.used = :used where uc.id = :id")
    void updateUsedStatus(@Param("id") Long id, @Param("used") boolean used);
}
