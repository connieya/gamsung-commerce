package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponFacadeTest {

    @InjectMocks
    CouponFacade couponFacade;

    @Mock
    UserService userService;

    @Mock
    CouponService couponService;

    @Mock
    UserCouponService userCouponService;

    private static final ZonedDateTime VALID_FROM = ZonedDateTime.now().minusDays(30);
    private static final ZonedDateTime VALID_TO = ZonedDateTime.now().plusDays(30);

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .userId("testuser")
                .email("test@test.com")
                .birthDate(new BirthDate("1990-01-01"))
                .gender(Gender.MALE)
                .build();
    }

    @Nested
    @DisplayName("쿠폰 발급 (issue)")
    class IssueTest {

        @Test
        @DisplayName("쿠폰 발급에 성공하면 발급 정보를 반환한다.")
        void issue_success() {
            // given
            User user = createTestUser();
            Coupon coupon = Coupon.create("신규 가입 쿠폰", "WELCOME-1", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, null);
            UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);

            when(userService.findByUserId("testuser")).thenReturn(user);
            when(couponService.issue(any(CouponCommand.Issue.class))).thenReturn(userCoupon);
            when(couponService.getCoupon(1L)).thenReturn(coupon);

            // when
            CouponCriteria.Issue criteria = new CouponCriteria.Issue("testuser", 1L);
            CouponResult.Issued result = couponFacade.issue(criteria);

            // then
            assertThat(result.couponName()).isEqualTo("신규 가입 쿠폰");
            assertThat(result.couponType()).isEqualTo(CouponType.FIXED_AMOUNT);
            assertThat(result.value()).isEqualTo(5000L);
            assertThat(result.expiredAt()).isEqualTo(VALID_TO);
        }
    }

    @Nested
    @DisplayName("쿠폰 받기 (claim)")
    class ClaimTest {

        @Test
        @DisplayName("쿠폰 코드로 쿠폰을 받으면 발급 정보를 반환한다.")
        void claim_success() {
            // given
            User user = createTestUser();
            UserCoupon userCoupon = UserCoupon.create(1L, 1L, VALID_TO);

            when(userService.findByUserId("testuser")).thenReturn(user);
            when(couponService.claim(any(CouponCommand.Claim.class))).thenReturn(userCoupon);
            Coupon coupon = Coupon.create("신규 가입 쿠폰", "WELCOME-CLAIM", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, null);
            when(couponService.getCoupon(1L)).thenReturn(coupon);

            // when
            CouponCriteria.Claim criteria = new CouponCriteria.Claim("testuser", "WELCOME-CLAIM");
            CouponResult.Claimed result = couponFacade.claim(criteria);

            // then
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.couponId()).isEqualTo(coupon.getId());
        }
    }

    @Nested
    @DisplayName("받을 수 있는 쿠폰 목록 조회 (getAvailableCoupons)")
    class GetAvailableCouponsTest {

        @Test
        @DisplayName("유효한 쿠폰 중 아직 받지 않은 쿠폰만 반환한다.")
        void getAvailableCoupons_excludesAlreadyIssued() {
            // given
            User user = createTestUser();
            Coupon coupon1 = Coupon.create("쿠폰A", "AVAIL-A", CouponType.FIXED_AMOUNT, 3000L,
                    VALID_FROM, VALID_TO, null);
            Coupon coupon2 = Coupon.create("쿠폰B", "AVAIL-B", CouponType.PERCENTAGE, 10L,
                    VALID_FROM, VALID_TO, null);
            Coupon coupon3 = Coupon.create("쿠폰C", "AVAIL-C", CouponType.FIXED_AMOUNT, 5000L,
                    VALID_FROM, VALID_TO, null);
            ReflectionTestUtils.setField(coupon1, "id", 1L);
            ReflectionTestUtils.setField(coupon2, "id", 2L);
            ReflectionTestUtils.setField(coupon3, "id", 3L);
            // 사용자가 coupon1(id=1)은 이미 받음
            UserCoupon issuedCoupon = UserCoupon.create(1L, coupon1.getId(), VALID_TO);

            when(userService.findByUserId("testuser")).thenReturn(user);
            when(couponService.getValidCoupons()).thenReturn(List.of(coupon1, coupon2, coupon3));
            when(userCouponService.getUserCoupons(1L)).thenReturn(List.of(issuedCoupon));

            // when
            CouponResult.AvailableCoupons result = couponFacade.getAvailableCoupons("testuser");

            // then
            assertThat(result.coupons()).hasSize(2);
        }

        @Test
        @DisplayName("받은 쿠폰이 없으면 유효한 쿠폰 전체를 반환한다.")
        void getAvailableCoupons_returnsAll_whenNoneIssued() {
            // given
            User user = createTestUser();
            Coupon coupon1 = Coupon.create("쿠폰A", "ALL-A", CouponType.FIXED_AMOUNT, 3000L,
                    VALID_FROM, VALID_TO, null);
            Coupon coupon2 = Coupon.create("쿠폰B", "ALL-B", CouponType.PERCENTAGE, 10L,
                    VALID_FROM, VALID_TO, null);
            ReflectionTestUtils.setField(coupon1, "id", 1L);
            ReflectionTestUtils.setField(coupon2, "id", 2L);

            when(userService.findByUserId("testuser")).thenReturn(user);
            when(couponService.getValidCoupons()).thenReturn(List.of(coupon1, coupon2));
            when(userCouponService.getUserCoupons(1L)).thenReturn(List.of());

            // when
            CouponResult.AvailableCoupons result = couponFacade.getAvailableCoupons("testuser");

            // then
            assertThat(result.coupons()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("보유 쿠폰 목록 조회 (getMyCoupons)")
    class GetMyCouponsTest {

        @Test
        @DisplayName("사용자의 보유 쿠폰 목록을 반환한다.")
        void getMyCoupons_success() {
            // given
            User user = createTestUser();
            UserCoupon userCoupon1 = UserCoupon.create(1L, 1L, VALID_TO);
            UserCoupon userCoupon2 = UserCoupon.create(1L, 2L, VALID_TO);
            Coupon coupon1 = Coupon.create("쿠폰A", "COUPON-A", CouponType.FIXED_AMOUNT, 3000L,
                    VALID_FROM, VALID_TO, null);
            Coupon coupon2 = Coupon.create("쿠폰B", "COUPON-B", CouponType.PERCENTAGE, 10L,
                    VALID_FROM, VALID_TO, null);
            ReflectionTestUtils.setField(coupon1, "id", 1L);
            ReflectionTestUtils.setField(coupon2, "id", 2L);

            when(userService.findByUserId("testuser")).thenReturn(user);
            when(userCouponService.getUserCoupons(1L)).thenReturn(List.of(userCoupon1, userCoupon2));
            when(couponService.getCoupons(List.of(1L, 2L))).thenReturn(List.of(coupon1, coupon2));

            // when
            CouponResult.MyCoupons result = couponFacade.getMyCoupons("testuser");

            // then
            assertThat(result.coupons()).hasSize(2);
        }

        @Test
        @DisplayName("보유 쿠폰이 없으면 빈 목록을 반환한다.")
        void getMyCoupons_returnsEmpty_whenNoCoupons() {
            // given
            User user = createTestUser();
            when(userService.findByUserId("testuser")).thenReturn(user);
            when(userCouponService.getUserCoupons(1L)).thenReturn(List.of());

            // when
            CouponResult.MyCoupons result = couponFacade.getMyCoupons("testuser");

            // then
            assertThat(result.coupons()).isEmpty();
        }
    }
}
