package com.loopers.domain.coupon;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    Optional<Coupon> findById(Long couponId);

    Optional<Coupon> findByCouponCode(String couponCode);

    List<Coupon> findAllByIdIn(List<Long> couponIds);

    List<Coupon> findAllWithinValidPeriod(ZonedDateTime now);

    Coupon save(Coupon coupon);
}
