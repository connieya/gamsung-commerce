package com.loopers.infrastructure.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponType;
import jakarta.persistence.*;


@Entity
@Table(name = "coupon")
public class CouponEntity extends BaseEntity {

    @Column(name = "coupon_name" , nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private Long value;


    public static CouponEntity from(Coupon coupon) {
        CouponEntity couponEntity = new CouponEntity();

        couponEntity.name = coupon.getName();
        couponEntity.couponType = coupon.getCouponType();
        couponEntity.value = coupon.getValue();

        return couponEntity;
    }


    public Coupon toDomain() {
        return Coupon
                .builder()
                .id(id)
                .couponType(couponType)
                .name(name)
                .value(value)
                .build();
    }

}
