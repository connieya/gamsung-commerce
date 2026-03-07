package com.loopers.domain.coupon;

import com.loopers.domain.coupon.exception.CouponException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @InjectMocks
    UserCouponService userCouponService;

    @Mock
    UserCouponRepository userCouponRepository;

    private static final ZonedDateTime VALID_TO = ZonedDateTime.now().plusDays(30);

    @Nested
    @DisplayName("쿠폰 사용 (use)")
    class UseTest {

        @Test
        @DisplayName("유효한 쿠폰을 정상 사용 처리한다.")
        void use_success() {
            // given
            UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);
            when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(Optional.of(userCoupon));

            // when
            userCouponService.use(1L, 1L);

            // then
            assertThat(userCoupon.isUsed()).isTrue();
            verify(userCouponRepository).save(userCoupon);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 쿠폰 사용 시 예외가 발생한다.")
        void use_throwsException_whenUserCouponNotFound() {
            // given
            when(userCouponRepository.findByUserIdAndCouponId(1L, 999L)).thenReturn(Optional.empty());

            // when && then
            assertThatThrownBy(() -> userCouponService.use(1L, 999L))
                    .isInstanceOf(CouponException.UserCouponNotFoundException.class);
        }

        @Test
        @DisplayName("이미 사용된 쿠폰 사용 시 예외가 발생한다.")
        void use_throwsException_whenAlreadyUsed() {
            // given
            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(1L)
                    .couponId(1L)
                    .used(true)
                    .expiredAt(VALID_TO)
                    .build();
            when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(Optional.of(userCoupon));

            // when && then
            assertThatThrownBy(() -> userCouponService.use(1L, 1L))
                    .isInstanceOf(CouponException.UserCouponAlreadyUsedException.class);
        }
    }

    @Nested
    @DisplayName("보유 쿠폰 조회 (getUserCoupons)")
    class GetUserCouponsTest {

        @Test
        @DisplayName("사용자의 보유 쿠폰 목록을 조회한다.")
        void getUserCoupons_success() {
            // given
            UserCoupon userCoupon1 = UserCoupon.create(1L, 1L, VALID_TO);
            UserCoupon userCoupon2 = UserCoupon.create(1L, 2L, VALID_TO);
            when(userCouponRepository.findAllByUserId(1L)).thenReturn(List.of(userCoupon1, userCoupon2));

            // when
            List<UserCoupon> result = userCouponService.getUserCoupons(1L);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("보유 쿠폰이 없으면 빈 목록을 반환한다.")
        void getUserCoupons_returnsEmptyList_whenNoCoupons() {
            // given
            when(userCouponRepository.findAllByUserId(1L)).thenReturn(List.of());

            // when
            List<UserCoupon> result = userCouponService.getUserCoupons(1L);

            // then
            assertThat(result).isEmpty();
        }
    }
}
