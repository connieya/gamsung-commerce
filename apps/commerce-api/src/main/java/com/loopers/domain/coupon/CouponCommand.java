package com.loopers.domain.coupon;

public class CouponCommand {

    public record Issue(Long userId, Long couponId) {
    }

    public record Claim(Long userId, String couponCode) {
    }
}
