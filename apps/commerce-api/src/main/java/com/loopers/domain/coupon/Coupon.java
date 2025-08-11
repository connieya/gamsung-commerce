package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Coupon {

    private Long id;
    private String name;
    private CouponType couponType;
    private Long value;


    @Builder
    private Coupon(Long id, String name, CouponType couponType, Long value) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름이 올바르지 않습니다.");
        }

        if (couponType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 타입이 올바르지 않습니다.");
        }

        if (value == null || value < 0L) {
            throw new CoreException(ErrorType.BAD_REQUEST , "유효하지 않는 쿠폰 할인 값입니다.");
        }

        this.id = id;
        this.name = name;
        this.couponType = couponType;
        this.value = value;
    }

    public static Coupon create(String name, CouponType couponType, Long value) {
        return Coupon
                .builder()
                .name(name)
                .couponType(couponType)
                .value(value)
                .build();
    }

    public Long calculateDiscountAmount(Long orderAmount) {
        return couponType.calculate(orderAmount, value);
    }
}
