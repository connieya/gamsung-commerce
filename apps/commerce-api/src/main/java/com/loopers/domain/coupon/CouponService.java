package com.loopers.domain.coupon;

import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public Long calculateDiscountAmount(Long couponId , Long orderAmount) {
        return couponRepository.findById(couponId)
                .map(coupon -> {
                    UserCoupon userCoupon = userCouponRepository.findByCouponId(couponId)
                            .orElseThrow(() -> new CouponException.UserCouponNotFoundException(ErrorType.USER_COUPON_NOT_FOUND));

                    if (!userCoupon.canUse()) {
                        throw new CouponException.UserCouponAlreadyUsedException(ErrorType.USER_COUPON_ALREADY_USED);
                    }
                    return coupon.calculateDiscountAmount(orderAmount);
                })
                .orElse(0L); // coupon 이 없으면 할인 없음

    }
}
