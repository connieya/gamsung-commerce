package com.loopers.domain.coupon;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UserCoupon {
    private Long id;
    private Long userId;
    private Long couponId;
    private boolean used;


    @Builder
    private UserCoupon(Long id, Long userId, Long couponId, boolean used) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.used = used;
    }

    public static UserCoupon create(Long userId, Long couponId) {
        return UserCoupon
                .builder()
                .userId(userId)
                .couponId(couponId)
                .used(false)
                .build();
    }


    public void use() {
        this.used = true;
    }


    public boolean canUse() {
        return !used;
    }
}
