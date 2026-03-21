package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    List<Coupon> findAllByIdIn(List<Long> ids);

    @Query("SELECT c FROM Coupon c WHERE c.validFrom <= :now AND c.validTo > :now")
    List<Coupon> findAllWithinValidPeriod(@Param("now") ZonedDateTime now);
}
