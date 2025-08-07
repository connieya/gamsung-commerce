package com.loopers.domain.coupon;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserCouponCommand {
    private String userId;

    @Builder
    private UserCouponCommand(String userId) {
        this.userId = userId;
    }

    public static UserCouponCommand of(String userId){
        return new UserCouponCommand(userId);
    }
}
