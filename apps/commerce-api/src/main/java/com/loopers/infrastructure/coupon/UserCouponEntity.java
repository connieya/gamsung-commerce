package com.loopers.infrastructure.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.infrastructure.user.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_coupon",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"couponId", "userId"}) // <-- 이 부분이 핵심입니다.
        })
public class UserCouponEntity extends BaseEntity {

    private Long couponId;
    private Long userId;
    private boolean used;


    public static UserCouponEntity fromDomain(UserCoupon userCoupon) {
        UserCouponEntity userCouponEntity = new UserCouponEntity();

        userCouponEntity.couponId = userCoupon.getCouponId();
        userCouponEntity.userId = userCoupon.getUserId();
        userCouponEntity.used = false;

        return userCouponEntity;
    }


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
