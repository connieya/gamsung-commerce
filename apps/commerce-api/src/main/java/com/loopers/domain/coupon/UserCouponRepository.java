package com.loopers.domain.coupon;

import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCoupon> findByCouponId(Long couponId);
}
