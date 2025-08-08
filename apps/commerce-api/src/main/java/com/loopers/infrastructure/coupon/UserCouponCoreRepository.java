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
        if (userCoupon.getId() != null) {
            Optional<UserCouponEntity> optionalUserCouponEntity = userCouponJpaRepository.findById(userCoupon.getId());
            if (optionalUserCouponEntity.isPresent()) {
                UserCouponEntity userCouponEntity = optionalUserCouponEntity.get();
                userCouponEntity.used(userCoupon.isUsed());
                userCouponJpaRepository.save(userCouponEntity);
                return userCoupon;

            }
        }
        return userCouponJpaRepository.save(UserCouponEntity.fromDomain(userCoupon))
                .toDomain();
    }

}
