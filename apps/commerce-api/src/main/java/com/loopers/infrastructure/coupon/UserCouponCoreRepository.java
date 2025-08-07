package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCouponCoreRepository implements UserCouponRepository {

    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public Optional<UserCoupon> findByCouponId(Long couponId) {
        return userCouponJpaRepository.findByCouponId(couponId)
                .map(UserCouponEntity::toDomain);
    }

    @Override
    public Optional<UserCoupon> findByUserId(Long id) {
        return userCouponJpaRepository.findByUserId(id)
                .map(UserCouponEntity::toDomain);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return null;
    }


    @Override
    public void updateUsedStatus(Long id, boolean used) {
        userCouponJpaRepository.updateUsedStatus(id ,used);
    }
}
