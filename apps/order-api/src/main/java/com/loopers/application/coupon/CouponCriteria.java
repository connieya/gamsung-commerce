package com.loopers.application.coupon;

public class CouponCriteria {

    public record Issue(String userId, Long couponId) {
    }

    public record Claim(String userId, String couponCode) {
    }
}
