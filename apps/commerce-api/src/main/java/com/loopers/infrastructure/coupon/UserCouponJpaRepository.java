package com.loopers.infrastructure.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity , Long> {
    Optional<UserCouponEntity> findByCouponId(Long couponId);
}
