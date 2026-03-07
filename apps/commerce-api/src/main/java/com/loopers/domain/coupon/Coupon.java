package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;


@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Column(name = "coupon_name" , nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private Long value;

    @Column(nullable = false)
    private ZonedDateTime validFrom;

    @Column(nullable = false)
    private ZonedDateTime validTo;

    private Integer validDays;

    @Builder
    private Coupon(String name, String couponCode, CouponType couponType, Long value,
                   ZonedDateTime validFrom, ZonedDateTime validTo, Integer validDays) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름이 올바르지 않습니다.");
        }

        if (!StringUtils.hasText(couponCode)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 코드는 필수입니다.");
        }

        if (couponType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 타입이 올바르지 않습니다.");
        }

        if (value == null || value < 0L) {
            throw new CoreException(ErrorType.BAD_REQUEST , "유효하지 않는 쿠폰 할인 값입니다.");
        }

        if (validFrom == null || validTo == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 유효기간은 필수입니다.");
        }

        if (!validFrom.isBefore(validTo)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 시작일은 종료일보다 이전이어야 합니다.");
        }

        this.name = name;
        this.couponCode = couponCode;
        this.couponType = couponType;
        this.value = value;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.validDays = validDays;
    }

    public static Coupon create(String name, String couponCode, CouponType couponType, Long value,
                                ZonedDateTime validFrom, ZonedDateTime validTo, Integer validDays) {
        return Coupon
                .builder()
                .name(name)
                .couponCode(couponCode)
                .couponType(couponType)
                .value(value)
                .validFrom(validFrom)
                .validTo(validTo)
                .validDays(validDays)
                .build();
    }

    public boolean isWithinValidPeriod() {
        ZonedDateTime now = ZonedDateTime.now();
        return !now.isBefore(validFrom) && now.isBefore(validTo);
    }

    public ZonedDateTime calculateExpiredAt(ZonedDateTime issuedAt) {
        if (validDays != null) {
            return issuedAt.plusDays(validDays);
        }
        return validTo;
    }

    public Long calculateDiscountAmount(Long orderAmount) {
        return couponType.calculate(orderAmount, value);
    }

}
