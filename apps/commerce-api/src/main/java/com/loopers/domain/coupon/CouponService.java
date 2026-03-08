package com.loopers.domain.coupon;

import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public Long calculateDiscountAmount(Long userId, Long couponId, Long orderAmount) {
        if (couponId == null) {
            return 0L;
        }
        return couponRepository.findById(couponId)
                .map(coupon -> {
                    if (!coupon.isWithinValidPeriod()) {
                        throw new CouponException.CouponExpiredException(ErrorType.COUPON_EXPIRED);
                    }

                    UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                            .orElseThrow(() -> new CouponException.UserCouponNotFoundException(ErrorType.USER_COUPON_NOT_FOUND));

                    if (!userCoupon.canUse()) {
                        throw new CouponException.UserCouponAlreadyUsedException(ErrorType.USER_COUPON_ALREADY_USED);
                    }
                    return coupon.calculateDiscountAmount(orderAmount);
                })
                .orElse(0L); // coupon 이 없으면 할인 없음

    }

    @Transactional
    public UserCoupon issue(CouponCommand.Issue command) {
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new CouponException.CouponNotFoundException(ErrorType.COUPON_NOT_FOUND));

        if (!coupon.isWithinValidPeriod()) {
            throw new CouponException.CouponExpiredException(ErrorType.COUPON_EXPIRED);
        }

        userCouponRepository.findByUserIdAndCouponId(command.userId(), command.couponId())
                .ifPresent(uc -> {
                    throw new CouponException.CouponAlreadyIssuedException(ErrorType.COUPON_ALREADY_ISSUED);
                });

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiredAt = coupon.calculateExpiredAt(now);
        UserCoupon userCoupon = UserCoupon.create(command.userId(), command.couponId(), expiredAt);
        return userCouponRepository.save(userCoupon);
    }

    @Transactional
    public UserCoupon claim(CouponCommand.Claim command) {
        Coupon coupon = couponRepository.findByCouponCode(command.couponCode())
                .orElseThrow(() -> new CouponException.CouponNotFoundException(ErrorType.COUPON_NOT_FOUND));

        if (!coupon.isWithinValidPeriod()) {
            throw new CouponException.CouponExpiredException(ErrorType.COUPON_EXPIRED);
        }

        userCouponRepository.findByUserIdAndCouponId(command.userId(), coupon.getId())
                .ifPresent(uc -> {
                    throw new CouponException.CouponAlreadyIssuedException(ErrorType.COUPON_ALREADY_ISSUED);
                });

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiredAt = coupon.calculateExpiredAt(now);
        UserCoupon userCoupon = UserCoupon.create(command.userId(), coupon.getId(), expiredAt);
        return userCouponRepository.save(userCoupon);
    }

    @Transactional(readOnly = true)
    public List<Coupon> getValidCoupons() {
        return couponRepository.findAllWithinValidPeriod(ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException.CouponNotFoundException(ErrorType.COUPON_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Coupon> getCoupons(List<Long> couponIds) {
        return couponRepository.findAllByIdIn(couponIds);
    }
}
