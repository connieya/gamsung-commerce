package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponResult;
import com.loopers.domain.coupon.CouponType;

import java.time.ZonedDateTime;
import java.util.List;

public class CouponV1Dto {

    public static class Request {

        public record Claim(String couponCode) {
        }
    }

    public static class Response {

        public record Claimed(
                Long userId,
                Long couponId,
                Long userCouponId
        ) {
            public static Claimed from(CouponResult.Claimed result) {
                return new Claimed(
                        result.userId(),
                        result.couponId(),
                        result.userCouponId()
                );
            }
        }

        public record Issued(
                Long userCouponId,
                String couponName,
                CouponType couponType,
                Long value,
                ZonedDateTime expiredAt
        ) {
            public static Issued from(CouponResult.Issued result) {
                return new Issued(
                        result.userCouponId(),
                        result.couponName(),
                        result.couponType(),
                        result.value(),
                        result.expiredAt()
                );
            }
        }

        public record AvailableCoupons(List<AvailableCoupon> coupons) {
            public static AvailableCoupons from(CouponResult.AvailableCoupons result) {
                List<AvailableCoupon> coupons = result.coupons().stream()
                        .map(AvailableCoupon::from)
                        .toList();
                return new AvailableCoupons(coupons);
            }
        }

        public record AvailableCoupon(
                Long couponId,
                String couponCode,
                String couponName,
                CouponType couponType,
                Long value,
                ZonedDateTime validTo
        ) {
            public static AvailableCoupon from(CouponResult.AvailableCoupon result) {
                return new AvailableCoupon(
                        result.couponId(),
                        result.couponCode(),
                        result.couponName(),
                        result.couponType(),
                        result.value(),
                        result.validTo()
                );
            }
        }

        public record MyCoupons(List<MyCoupon> coupons) {
            public static MyCoupons from(CouponResult.MyCoupons result) {
                List<MyCoupon> coupons = result.coupons().stream()
                        .map(MyCoupon::from)
                        .toList();
                return new MyCoupons(coupons);
            }
        }

        public record MyCoupon(
                String couponName,
                CouponType couponType,
                Long value,
                boolean used,
                boolean expired,
                ZonedDateTime expiredAt
        ) {
            public static MyCoupon from(CouponResult.MyCoupon result) {
                return new MyCoupon(
                        result.couponName(),
                        result.couponType(),
                        result.value(),
                        result.used(),
                        result.expired(),
                        result.expiredAt()
                );
            }
        }
    }
}
