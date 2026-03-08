package com.loopers.interfaces.api.internal;

import com.loopers.domain.coupon.CouponService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/coupons")
@RequiredArgsConstructor
public class InternalCouponController {

    private final CouponService couponService;

    @PostMapping("/calculate-discount")
    public ApiResponse<InternalDto.CouponDiscountResponse> calculateDiscount(@RequestBody InternalDto.CouponDiscountRequest request) {
        Long discountAmount = couponService.calculateDiscountAmount(request.userId(), request.couponId(), request.totalAmount());
        return ApiResponse.success(new InternalDto.CouponDiscountResponse(discountAmount));
    }
}
