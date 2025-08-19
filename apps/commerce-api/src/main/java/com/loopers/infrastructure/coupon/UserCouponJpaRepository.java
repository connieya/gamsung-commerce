package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon, Long> {
    Optional<UserCoupon> findByCouponId(Long couponId);

    Optional<UserCoupon> findByUserId(Long userId);

    @Modifying
    @Query("update UserCoupon uc set uc.used = :used where uc.id = :id")
    void updateUsedStatus(@Param("id") Long id, @Param("used") boolean used);
}
