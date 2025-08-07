package com.loopers.domain.coupon;

import com.loopers.domain.coupon.exception.CouponException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    CouponService couponService;

    @Mock
    CouponRepository couponRepository;

    @Mock
    UserCouponRepository userCouponRepository;

    @Test
    @DisplayName("쿠폰은 사용자가 소유하고 있으며, 이미 사용된 쿠폰은 사용할 수 없어야 한다.")
    void getDiscountAmount_throwsException_whenCouponAlreadyUsed() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .couponId(1L)
                .userId(1L)
                .used(true)
                .build();
        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(
                        Coupon.create("여름 이벤트 ", CouponType.FIXED_AMOUNT,10000L)
                ));

        when(userCouponRepository.findByCouponId(1L))
                .thenReturn(Optional.of(userCoupon));

        // when && then
        assertThatThrownBy(
                () -> {
                    couponService.getDiscountAmount(1L, 1000L);
                }
        ).isInstanceOf(CouponException.UserCouponAlreadyUsedException.class);

    }

    @Test
    @DisplayName("주문 시 쿠폰이 없으면 할인 금액은 0원이다.")
    void getDiscountAmount_returnsZero_whenCouponDoesNotExist() {
        when(couponRepository.findById(1L))
                .thenReturn(Optional.empty());

        Long discountAmount = couponService.getDiscountAmount(1L, 1000L);
        assertThat(discountAmount).isEqualTo(0L);
    }

    @Test
    @DisplayName("정액 할인 쿠폰 적용 시, 할인 금액이 올바르게 계산된다.")
    void getDiscountAmount_calculatesCorrectly_whenFixedAmountCouponApplied() {
        // given
        Coupon coupon = Coupon.create("여름 이벤트", CouponType.FIXED_AMOUNT, 10000L);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        when(userCouponRepository.findByCouponId(1L))
                .thenReturn(Optional.of(userCoupon));
        // when
        Long discountAmount = couponService.getDiscountAmount(1L, 50000L);


        // then
        // 주문 금액은 5만원 , 정액 할인 쿠폰은  1만원
        assertThat(discountAmount).isEqualTo(10000L);

    }


    @Test
    @DisplayName("정액 할인 금액이 주문 금액보다 클 경우, 주문 금액 전액이 할인 금액으로 적용된다.")
    void getDiscountAmount_appliesFullOrderAmount_whenFixedAmountExceedsTotal() {
        // given
        Coupon coupon = Coupon.create("여름 이벤트", CouponType.FIXED_AMOUNT, 30000L);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        when(userCouponRepository.findByCouponId(1L))
                .thenReturn(Optional.of(userCoupon));
        // when
        Long discountAmount = couponService.getDiscountAmount(1L, 25000L);


        // then
        // 주문 금액은 25,000원 , 정액 할인 쿠폰은  30,000원
        assertThat(discountAmount).isEqualTo(25000L);

    }


    @Test
    @DisplayName("정률 할인 쿠폰 적용 시, 할인 금액이 올바르게 계산된다")
    void getDiscountAmount_calculatesCorrectly_whenPercentageCouponApplied() {
        // given
        Coupon coupon = Coupon.create("여름 이벤트", CouponType.PERCENTAGE, 20L);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        when(userCouponRepository.findByCouponId(1L))
                .thenReturn(Optional.of(userCoupon));
        // when
        Long discountAmount = couponService.getDiscountAmount(1L, 40000L);


        // then
        // 주문 금액은 40,000원 , 정률 할인 쿠폰은  20%
        assertThat(discountAmount).isEqualTo(8000L);
    }

}
