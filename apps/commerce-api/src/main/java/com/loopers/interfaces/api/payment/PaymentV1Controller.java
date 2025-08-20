package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResult;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentV1Controller {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ApiResponse<PaymentV1Dto.Response.Pay> payment(@RequestHeader(ApiHeaders.USER_ID) String userId , PaymentV1Dto.Request.Pay request) {
        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(
                userId,
                request.orderId(),
                request.paymentMethod()
        );
        PaymentResult paymentResult = paymentFacade.pay(criteria);
        return ApiResponse.success(PaymentV1Dto.Response.Pay.from(paymentResult));
    }
}
