package com.loopers.interfaces.api.coupon;

import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Coupon V1 API", description = "쿠폰 관련 API 입니다.")
public interface CouponV1ApiSpec {

    @Operation(summary = "쿠폰 발급", description = "쿠폰 ID로 사용자에게 쿠폰을 발급합니다.")
    ApiResponse<CouponV1Dto.Response.Issued> issue(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("couponId") Long couponId
    );

    @Operation(summary = "쿠폰 받기", description = "쿠폰 코드로 쿠폰을 받습니다.")
    ApiResponse<CouponV1Dto.Response.Claimed> claim(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody CouponV1Dto.Request.Claim request
    );

    @Operation(summary = "받을 수 있는 쿠폰 목록 조회", description = "현재 유효하며 아직 받지 않은 쿠폰 목록을 조회합니다.")
    ApiResponse<CouponV1Dto.Response.AvailableCoupons> getAvailableCoupons(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );

    @Operation(summary = "보유 쿠폰 목록 조회", description = "사용자가 보유한 쿠폰 목록을 조회합니다.")
    ApiResponse<CouponV1Dto.Response.MyCoupons> getMyCoupons(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );
}
