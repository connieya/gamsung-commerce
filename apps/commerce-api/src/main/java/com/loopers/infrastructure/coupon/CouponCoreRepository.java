package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponCoreRepository implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Override
    public Optional<Coupon> findByCouponCode(String couponCode) {
        return couponJpaRepository.findByCouponCode(couponCode);
    }

    @Override
    public List<Coupon> findAllByIdIn(List<Long> couponIds) {
        return couponJpaRepository.findAllByIdIn(couponIds);
    }

    @Override
    public List<Coupon> findAllWithinValidPeriod(ZonedDateTime now) {
        return couponJpaRepository.findAllWithinValidPeriod(now);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }
}
