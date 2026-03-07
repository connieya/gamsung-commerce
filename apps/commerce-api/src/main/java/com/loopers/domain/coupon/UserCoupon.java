package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_coupon",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"couponId", "userId"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    private Long couponId;
    private Long userId;
    private boolean used;

    @Column(nullable = false)
    private ZonedDateTime expiredAt;

    @Version
    private Long version;

    @Builder
    public UserCoupon(Long couponId, Long userId, boolean used, ZonedDateTime expiredAt) {
        this.couponId = couponId;
        this.userId = userId;
        this.used = used;
        this.expiredAt = expiredAt;
    }

    public static UserCoupon create(Long userId, Long couponId, ZonedDateTime expiredAt) {
        return UserCoupon
                .builder()
                .userId(userId)
                .couponId(couponId)
                .used(false)
                .expiredAt(expiredAt)
                .build();
    }

    public void use() {
        this.used = true;
    }

    public boolean isExpired() {
        return !ZonedDateTime.now().isBefore(expiredAt);
    }

    public boolean canUse() {
        return !used && !isExpired();
    }
}
