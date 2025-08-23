package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_coupon",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"couponId", "userId"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    private Long couponId;
    private Long userId;
    private boolean used;

    @Version
    private Long version;

    @Builder
    public UserCoupon(Long couponId, Long userId, boolean used) {
        this.couponId = couponId;
        this.userId = userId;
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
