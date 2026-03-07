package com.loopers.domain.coupon;

import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public void use(Long userId, Long couponId) {
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new CouponException.UserCouponNotFoundException(ErrorType.USER_COUPON_NOT_FOUND));

        if (!userCoupon.canUse()) {
            throw new CouponException.UserCouponAlreadyUsedException(ErrorType.USER_COUPON_ALREADY_USED);
        }
        userCoupon.use();
        userCouponRepository.save(userCoupon);
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> getUserCoupons(Long userId) {
        return userCouponRepository.findAllByUserId(userId);
    }
}
