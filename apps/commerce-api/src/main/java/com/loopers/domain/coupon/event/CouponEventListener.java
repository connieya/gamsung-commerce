package com.loopers.domain.coupon.event;

import com.loopers.domain.coupon.UserCouponService;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.payment.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponEventListener {

    private final UserCouponService userCouponService;

    @EventListener
    public void handle(PaymentEvent.Success event) {
        if (event.couponId() == null) return;
        userCouponService.use(event.userId(), event.couponId());
    }

    @EventListener
    public void handle(PaymentEvent.Complete event) {
        if (event.couponId() == null) return;
        if (event.status() == TransactionStatus.SUCCESS) {
            userCouponService.use(event.userId(), event.couponId());
        }
    }
}
