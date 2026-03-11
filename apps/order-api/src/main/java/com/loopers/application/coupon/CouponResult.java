package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponType;

import java.time.ZonedDateTime;
import java.util.List;

public class CouponResult {

    public record Issued(
            Long userCouponId,
            String couponName,
            CouponType couponType,
            Long value,
            ZonedDateTime expiredAt
    ) {
    }

    public record Claimed(
            Long userId,
            Long couponId,
            Long userCouponId
    ) {
    }

    public record AvailableCoupons(List<AvailableCoupon> coupons) {
    }

    public record AvailableCoupon(
            Long couponId,
            String couponCode,
            String couponName,
            CouponType couponType,
            Long value,
            ZonedDateTime validTo
    ) {
    }

    public record MyCoupons(List<MyCoupon> coupons) {
    }

    public record MyCoupon(
            String couponName,
            CouponType couponType,
            Long value,
            boolean used,
            boolean expired,
            ZonedDateTime expiredAt
    ) {
    }
}
