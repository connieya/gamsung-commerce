package com.loopers.domain.coupon.event;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponEventListener {

    private final UserCouponRepository userCouponRepository;

    @EventListener
    public void handle(PaymentEvent.Success event) {
        UserCoupon userCoupon = userCouponRepository.findByCouponId(event.couponId())
                .orElseThrow(() -> new CouponException.UserCouponNotFoundException(ErrorType.USER_COUPON_NOT_FOUND));
        if (!userCoupon.canUse()) {
            throw new CouponException.UserCouponAlreadyUsedException(ErrorType.USER_COUPON_ALREADY_USED);
        }

        userCoupon.use();
        userCouponRepository.save(userCoupon);
    }

    @EventListener
    public void handle(PaymentEvent.Complete event) {
        TransactionStatus status = event.status();
        if (status  == TransactionStatus.SUCCESS) {
            UserCoupon userCoupon = userCouponRepository.findByCouponId(event.couponId())
                    .orElseThrow(() -> new CouponException.UserCouponNotFoundException(ErrorType.USER_COUPON_NOT_FOUND));
            if (!userCoupon.canUse()) {
                throw new CouponException.UserCouponAlreadyUsedException(ErrorType.USER_COUPON_ALREADY_USED);
            }

            userCoupon.use();
            userCouponRepository.save(userCoupon);
        }
    }
}
