package com.loopers.infrastructure.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.coupon.UserCoupon;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_coupon")
public class UserCouponEntity extends BaseEntity {

    private Long couponId;
    private Long userId;
    private boolean used;


    public UserCoupon toDomain() {
        return UserCoupon
                .builder()
                .id(id)
                .couponId(couponId)
                .userId(userId)
                .used(used)
                .build();
    }


}
