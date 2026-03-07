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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    CouponService couponService;

    @Mock
    CouponRepository couponRepository;

    @Mock
    UserCouponRepository userCouponRepository;

    private static final ZonedDateTime VALID_FROM = ZonedDateTime.now().minusDays(30);
    private static final ZonedDateTime VALID_TO = ZonedDateTime.now().plusDays(30);

    @Test
    @DisplayName("쿠폰은 사용자가 소유하고 있으며, 이미 사용된 쿠폰은 사용할 수 없어야 한다.")
    void calculateDiscountAmount_throwsException_whenCouponAlreadyUsed() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .couponId(1L)
                .userId(1L)
                .used(true)
                .expiredAt(VALID_TO)
                .build();
        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(
                        Coupon.create("여름 이벤트 ", "SUMMER-EVENT-1", CouponType.FIXED_AMOUNT, 10000L,
                                VALID_FROM, VALID_TO, null)
                ));

        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L))
                .thenReturn(Optional.of(userCoupon));

        // when && then
        assertThatThrownBy(
                () -> {
                    couponService.calculateDiscountAmount(1L, 1L, 1000L);
                }
        ).isInstanceOf(CouponException.UserCouponAlreadyUsedException.class);

    }

    @Test
    @DisplayName("주문 시 쿠폰이 없으면 할인 금액은 0원이다.")
    void calculateDiscountAmount_returnsZero_whenCouponDoesNotExist() {
        when(couponRepository.findById(1L))
                .thenReturn(Optional.empty());

        Long discountAmount = couponService.calculateDiscountAmount(1L, 1L, 1000L);
        assertThat(discountAmount).isEqualTo(0L);
    }

    @Test
    @DisplayName("정액 할인 쿠폰 적용 시, 할인 금액이 올바르게 계산된다.")
    void calculateDiscountAmount_calculatesCorrectly_whenFixedAmountCouponApplied() {
        // given
        Coupon coupon = Coupon.create("여름 이벤트", "SUMMER-EVENT-2", CouponType.FIXED_AMOUNT, 10000L,
                VALID_FROM, VALID_TO, null);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L))
                .thenReturn(Optional.of(userCoupon));
        // when
        Long discountAmount = couponService.calculateDiscountAmount(1L, 1L, 50000L);


        // then
        // 주문 금액은 5만원 , 정액 할인 쿠폰은  1만원
        assertThat(discountAmount).isEqualTo(10000L);

    }


    @Test
    @DisplayName("정액 할인 금액이 주문 금액보다 클 경우, 주문 금액 전액이 할인 금액으로 적용된다.")
    void calculateDiscountAmount_appliesFullOrderAmount_whenFixedAmountExceedsTotal() {
        // given
        Coupon coupon = Coupon.create("여름 이벤트", "SUMMER-EVENT-3", CouponType.FIXED_AMOUNT, 30000L,
                VALID_FROM, VALID_TO, null);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L))
                .thenReturn(Optional.of(userCoupon));
        // when
        Long discountAmount = couponService.calculateDiscountAmount(1L, 1L, 25000L);


        // then
        // 주문 금액은 25,000원 , 정액 할인 쿠폰은  30,000원
        assertThat(discountAmount).isEqualTo(25000L);

    }


    @Test
    @DisplayName("정률 할인 쿠폰 적용 시, 할인 금액이 올바르게 계산된다")
    void calculateDiscountAmount_calculatesCorrectly_whenPercentageCouponApplied() {
        // given
        Coupon coupon = Coupon.create("여름 이벤트", "SUMMER-PCT", CouponType.PERCENTAGE, 20L,
                VALID_FROM, VALID_TO, null);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L))
                .thenReturn(Optional.of(userCoupon));
        // when
        Long discountAmount = couponService.calculateDiscountAmount(1L, 1L, 40000L);


        // then
        // 주문 금액은 40,000원 , 정률 할인 쿠폰은  20%
        assertThat(discountAmount).isEqualTo(8000L);
    }

    @Test
    @DisplayName("만료된 쿠폰 사용 시 CouponExpiredException이 발생한다.")
    void calculateDiscountAmount_throwsException_whenCouponExpired() {
        // given
        ZonedDateTime expiredFrom = ZonedDateTime.now().minusDays(60);
        ZonedDateTime expiredTo = ZonedDateTime.now().minusDays(1);
        Coupon coupon = Coupon.create("만료 이벤트", "EXPIRED-1", CouponType.FIXED_AMOUNT, 5000L,
                expiredFrom, expiredTo, null);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));

        // when && then
        assertThatThrownBy(
                () -> couponService.calculateDiscountAmount(1L, 1L, 50000L)
        ).isInstanceOf(CouponException.CouponExpiredException.class);
    }

    @Test
    @DisplayName("유효기간 내 쿠폰은 정상적으로 할인이 적용된다.")
    void calculateDiscountAmount_calculatesCorrectly_whenCouponWithinValidPeriod() {
        // given
        Coupon coupon = Coupon.create("유효 이벤트", "VALID-1", CouponType.FIXED_AMOUNT, 5000L,
                VALID_FROM, VALID_TO, null);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);

        when(couponRepository.findById(1L))
                .thenReturn(Optional.of(coupon));
        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L))
                .thenReturn(Optional.of(userCoupon));

        // when
        Long discountAmount = couponService.calculateDiscountAmount(1L, 1L, 30000L);

        // then
        assertThat(discountAmount).isEqualTo(5000L);
    }

    @Test
    @DisplayName("validDays 기반으로 UserCoupon 만료일이 올바르게 계산된다.")
    void calculateExpiredAt_calculatesCorrectly_whenValidDaysProvided() {
        // given
        ZonedDateTime issuedAt = ZonedDateTime.now();
        Coupon coupon = Coupon.create("7일 쿠폰", "7DAY-1", CouponType.FIXED_AMOUNT, 3000L,
                VALID_FROM, VALID_TO, 7);

        // when
        ZonedDateTime expiredAt = coupon.calculateExpiredAt(issuedAt);

        // then
        assertThat(expiredAt).isEqualTo(issuedAt.plusDays(7));
    }

    @Test
    @DisplayName("validDays가 null이면 validTo가 만료일로 사용된다.")
    void calculateExpiredAt_usesValidTo_whenValidDaysIsNull() {
        // given
        ZonedDateTime issuedAt = ZonedDateTime.now();
        Coupon coupon = Coupon.create("기간 쿠폰", "PERIOD-1", CouponType.FIXED_AMOUNT, 3000L,
                VALID_FROM, VALID_TO, null);

        // when
        ZonedDateTime expiredAt = coupon.calculateExpiredAt(issuedAt);

        // then
        assertThat(expiredAt).isEqualTo(VALID_TO);
    }

    @Test
    @DisplayName("만료된 UserCoupon은 사용할 수 없다.")
    void canUse_returnsFalse_whenUserCouponExpired() {
        // given
        ZonedDateTime pastExpiredAt = ZonedDateTime.now().minusDays(1);
        UserCoupon userCoupon = UserCoupon.create(1L, 1L, pastExpiredAt);

        // when && then
        assertThat(userCoupon.canUse()).isFalse();
        assertThat(userCoupon.isExpired()).isTrue();
    }

    @Nested
    @DisplayName("쿠폰 발급 (issue)")
    class IssueTest {

        @Test
        @DisplayName("유효한 쿠폰을 정상 발급한다.")
        void issue_success() {
            // given
            Coupon coupon = Coupon.create("신규 가입 쿠폰", "WELCOME-1", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, 7);
            UserCoupon savedUserCoupon = UserCoupon.create(1L, 1L, VALID_TO);

            when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(Optional.empty());
            when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(savedUserCoupon);

            // when
            CouponCommand.Issue command = new CouponCommand.Issue(1L, 1L);
            UserCoupon result = couponService.issue(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCouponId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 발급 시 CouponNotFoundException이 발생한다.")
        void issue_throwsException_whenCouponNotFound() {
            // given
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());

            // when && then
            CouponCommand.Issue command = new CouponCommand.Issue(1L, 999L);
            assertThatThrownBy(() -> couponService.issue(command))
                    .isInstanceOf(CouponException.CouponNotFoundException.class);
        }

        @Test
        @DisplayName("만료된 쿠폰 발급 시 CouponExpiredException이 발생한다.")
        void issue_throwsException_whenCouponExpired() {
            // given
            ZonedDateTime expiredFrom = ZonedDateTime.now().minusDays(60);
            ZonedDateTime expiredTo = ZonedDateTime.now().minusDays(1);
            Coupon expiredCoupon = Coupon.create("만료 쿠폰", "EXPIRED-2", CouponType.FIXED_AMOUNT, 5000L,
                    expiredFrom, expiredTo, null);

            when(couponRepository.findById(1L)).thenReturn(Optional.of(expiredCoupon));

            // when && then
            CouponCommand.Issue command = new CouponCommand.Issue(1L, 1L);
            assertThatThrownBy(() -> couponService.issue(command))
                    .isInstanceOf(CouponException.CouponExpiredException.class);
        }

        @Test
        @DisplayName("이미 발급받은 쿠폰을 다시 발급 시 CouponAlreadyIssuedException이 발생한다.")
        void issue_throwsException_whenAlreadyIssued() {
            // given
            Coupon coupon = Coupon.create("여름 이벤트", "SUMMER-5K", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, null);
            UserCoupon existingUserCoupon = UserCoupon.create(1L, 1L, VALID_TO);

            when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(Optional.of(existingUserCoupon));

            // when && then
            CouponCommand.Issue command = new CouponCommand.Issue(1L, 1L);
            assertThatThrownBy(() -> couponService.issue(command))
                    .isInstanceOf(CouponException.CouponAlreadyIssuedException.class);
        }
    }

    @Nested
    @DisplayName("쿠폰 받기 (claim)")
    class ClaimTest {

        @Test
        @DisplayName("쿠폰 코드로 유효한 쿠폰을 받는다.")
        void claim_success() {
            // given
            Coupon coupon = Coupon.create("신규 가입 쿠폰", "WELCOME-CLAIM", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, null);
            UserCoupon savedUserCoupon = UserCoupon.create(1L, 1L, VALID_TO);

            when(couponRepository.findByCouponCode("WELCOME-CLAIM")).thenReturn(Optional.of(coupon));
            when(userCouponRepository.findByUserIdAndCouponId(1L, coupon.getId())).thenReturn(Optional.empty());
            when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(savedUserCoupon);

            // when
            CouponCommand.Claim command = new CouponCommand.Claim(1L, "WELCOME-CLAIM");
            UserCoupon result = couponService.claim(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 코드로 받기 시 CouponNotFoundException이 발생한다.")
        void claim_throwsException_whenCouponCodeNotFound() {
            // given
            when(couponRepository.findByCouponCode("INVALID-CODE")).thenReturn(Optional.empty());

            // when && then
            CouponCommand.Claim command = new CouponCommand.Claim(1L, "INVALID-CODE");
            assertThatThrownBy(() -> couponService.claim(command))
                    .isInstanceOf(CouponException.CouponNotFoundException.class);
        }

        @Test
        @DisplayName("만료된 쿠폰 코드로 받기 시 CouponExpiredException이 발생한다.")
        void claim_throwsException_whenCouponExpired() {
            // given
            ZonedDateTime expiredFrom = ZonedDateTime.now().minusDays(60);
            ZonedDateTime expiredTo = ZonedDateTime.now().minusDays(1);
            Coupon expiredCoupon = Coupon.create("만료 쿠폰", "EXPIRED-CLAIM", CouponType.FIXED_AMOUNT, 5000L,
                    expiredFrom, expiredTo, null);

            when(couponRepository.findByCouponCode("EXPIRED-CLAIM")).thenReturn(Optional.of(expiredCoupon));

            // when && then
            CouponCommand.Claim command = new CouponCommand.Claim(1L, "EXPIRED-CLAIM");
            assertThatThrownBy(() -> couponService.claim(command))
                    .isInstanceOf(CouponException.CouponExpiredException.class);
        }

        @Test
        @DisplayName("이미 받은 쿠폰 코드로 다시 받기 시 CouponAlreadyIssuedException이 발생한다.")
        void claim_throwsException_whenAlreadyClaimed() {
            // given
            Coupon coupon = Coupon.create("중복 쿠폰", "DUP-CLAIM", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, null);
            UserCoupon existing = UserCoupon.create(1L, coupon.getId(), VALID_TO);

            when(couponRepository.findByCouponCode("DUP-CLAIM")).thenReturn(Optional.of(coupon));
            when(userCouponRepository.findByUserIdAndCouponId(1L, coupon.getId())).thenReturn(Optional.of(existing));

            // when && then
            CouponCommand.Claim command = new CouponCommand.Claim(1L, "DUP-CLAIM");
            assertThatThrownBy(() -> couponService.claim(command))
                    .isInstanceOf(CouponException.CouponAlreadyIssuedException.class);
        }
    }

    @Nested
    @DisplayName("쿠폰 조회 (getCoupon, getCoupons)")
    class GetCouponTest {

        @Test
        @DisplayName("쿠폰 ID로 쿠폰을 조회한다.")
        void getCoupon_success() {
            // given
            Coupon coupon = Coupon.create("조회 쿠폰", "SEARCH-1", CouponType.PERCENTAGE, 15L,
                    VALID_FROM, VALID_TO, null);
            when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

            // when
            Coupon result = couponService.getCoupon(1L);

            // then
            assertThat(result.getName()).isEqualTo("조회 쿠폰");
            assertThat(result.getCouponType()).isEqualTo(CouponType.PERCENTAGE);
            assertThat(result.getValue()).isEqualTo(15L);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 조회 시 CouponNotFoundException이 발생한다.")
        void getCoupon_throwsException_whenNotFound() {
            // given
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());

            // when && then
            assertThatThrownBy(() -> couponService.getCoupon(999L))
                    .isInstanceOf(CouponException.CouponNotFoundException.class);
        }

        @Test
        @DisplayName("쿠폰 ID 목록으로 여러 쿠폰을 일괄 조회한다.")
        void getCoupons_success() {
            // given
            Coupon coupon1 = Coupon.create("쿠폰1", "COUPON-1", CouponType.FIXED_AMOUNT, 3000L,
                    VALID_FROM, VALID_TO, null);
            Coupon coupon2 = Coupon.create("쿠폰2", "COUPON-2", CouponType.PERCENTAGE, 10L,
                    VALID_FROM, VALID_TO, null);
            when(couponRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(coupon1, coupon2));

            // when
            List<Coupon> result = couponService.getCoupons(List.of(1L, 2L));

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("유효기간 내 쿠폰 목록을 조회한다.")
        void getValidCoupons_success() {
            // given
            Coupon coupon1 = Coupon.create("유효 쿠폰1", "VALID-C1", CouponType.FIXED_AMOUNT, 3000L,
                    VALID_FROM, VALID_TO, null);
            Coupon coupon2 = Coupon.create("유효 쿠폰2", "VALID-C2", CouponType.PERCENTAGE, 10L,
                    VALID_FROM, VALID_TO, null);
            when(couponRepository.findAllWithinValidPeriod(any(ZonedDateTime.class)))
                    .thenReturn(List.of(coupon1, coupon2));

            // when
            List<Coupon> result = couponService.getValidCoupons();

            // then
            assertThat(result).hasSize(2);
        }
    }

}
