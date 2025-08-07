package com.loopers.domain.coupon;

import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCoupon> findByCouponId(Long couponId);

    Optional<UserCoupon> findByUserId(Long id);

    UserCoupon save(UserCoupon userCoupon);

    void updateUsedStatus(Long id, boolean used);
}
