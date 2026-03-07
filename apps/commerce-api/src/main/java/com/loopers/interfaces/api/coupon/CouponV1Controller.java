package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.CouponResult;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponV1Controller implements CouponV1ApiSpec {

    private final CouponFacade couponFacade;

    @PostMapping("/{couponId}/issue")
    @Override
    public ApiResponse<CouponV1Dto.Response.Issued> issue(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("couponId") Long couponId
    ) {
        CouponCriteria.Issue criteria = new CouponCriteria.Issue(userId, couponId);
        CouponResult.Issued result = couponFacade.issue(criteria);
        return ApiResponse.success(CouponV1Dto.Response.Issued.from(result));
    }

    @PostMapping("/claim")
    @Override
    public ApiResponse<CouponV1Dto.Response.Claimed> claim(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody CouponV1Dto.Request.Claim request
    ) {
        CouponCriteria.Claim criteria = new CouponCriteria.Claim(userId, request.couponCode());
        CouponResult.Claimed result = couponFacade.claim(criteria);
        return ApiResponse.success(CouponV1Dto.Response.Claimed.from(result));
    }

    @GetMapping("/available")
    @Override
    public ApiResponse<CouponV1Dto.Response.AvailableCoupons> getAvailableCoupons(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        CouponResult.AvailableCoupons result = couponFacade.getAvailableCoupons(userId);
        return ApiResponse.success(CouponV1Dto.Response.AvailableCoupons.from(result));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<CouponV1Dto.Response.MyCoupons> getMyCoupons(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        CouponResult.MyCoupons result = couponFacade.getMyCoupons(userId);
        return ApiResponse.success(CouponV1Dto.Response.MyCoupons.from(result));
    }
}
