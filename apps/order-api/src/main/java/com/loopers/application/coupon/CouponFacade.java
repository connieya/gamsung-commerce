package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import com.loopers.infrastructure.feign.commerce.CommerceApiClient;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CommerceApiClient commerceApiClient;
    private final CouponService couponService;
    private final UserCouponService userCouponService;

    @Transactional
    public CouponResult.Issued issue(CouponCriteria.Issue criteria) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(criteria.userId()).data();

        CouponCommand.Issue command = new CouponCommand.Issue(user.id(), criteria.couponId());
        UserCoupon userCoupon = couponService.issue(command);

        Coupon coupon = couponService.getCoupon(criteria.couponId());

        return new CouponResult.Issued(
                userCoupon.getId(),
                coupon.getName(),
                coupon.getCouponType(),
                coupon.getValue(),
                userCoupon.getExpiredAt()
        );
    }

    @Transactional
    public CouponResult.Claimed claim(CouponCriteria.Claim criteria) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(criteria.userId()).data();

        CouponCommand.Claim command = new CouponCommand.Claim(user.id(), criteria.couponCode());
        UserCoupon userCoupon = couponService.claim(command);

        Coupon coupon = couponService.getCoupon(userCoupon.getCouponId());

        return new CouponResult.Claimed(
                user.id(),
                coupon.getId(),
                userCoupon.getId()
        );
    }

    @Transactional(readOnly = true)
    public CouponResult.AvailableCoupons getAvailableCoupons(String userId) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();

        List<Coupon> validCoupons = couponService.getValidCoupons();
        Set<Long> issuedCouponIds = userCouponService.getUserCoupons(user.id()).stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toSet());

        List<CouponResult.AvailableCoupon> availableCoupons = validCoupons.stream()
                .filter(coupon -> !issuedCouponIds.contains(coupon.getId()))
                .map(coupon -> new CouponResult.AvailableCoupon(
                        coupon.getId(),
                        coupon.getCouponCode(),
                        coupon.getName(),
                        coupon.getCouponType(),
                        coupon.getValue(),
                        coupon.getValidTo()
                ))
                .toList();

        return new CouponResult.AvailableCoupons(availableCoupons);
    }

    @Transactional(readOnly = true)
    public CouponResult.MyCoupons getMyCoupons(String userId) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();

        List<UserCoupon> userCoupons = userCouponService.getUserCoupons(user.id());
        if (userCoupons.isEmpty()) {
            return new CouponResult.MyCoupons(List.of());
        }

        List<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .toList();
        Map<Long, Coupon> couponMap = couponService.getCoupons(couponIds).stream()
                .collect(Collectors.toMap(Coupon::getId, Function.identity()));

        List<CouponResult.MyCoupon> myCoupons = userCoupons.stream()
                .map(uc -> {
                    Coupon coupon = couponMap.get(uc.getCouponId());
                    return new CouponResult.MyCoupon(
                            coupon.getName(),
                            coupon.getCouponType(),
                            coupon.getValue(),
                            uc.isUsed(),
                            uc.isExpired(),
                            uc.getExpiredAt()
                    );
                })
                .toList();

        return new CouponResult.MyCoupons(myCoupons);
    }
}
