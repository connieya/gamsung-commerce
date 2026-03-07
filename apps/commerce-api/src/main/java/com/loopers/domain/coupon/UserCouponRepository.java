package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCoupon> findByUserId(Long id);

    List<UserCoupon> findAllByUserId(Long userId);

    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    UserCoupon save(UserCoupon userCoupon);
}
