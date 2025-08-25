package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResult;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentV1Controller {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ApiResponse<Void> payment(@RequestHeader(ApiHeaders.USER_ID) String userId , @RequestBody PaymentV1Dto.Request.Pay request) {
        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(
                userId,
                request.orderId(),
                request.paymentMethod(),
                request.cardType(),
                request.cardNumber()
        );
        paymentFacade.pay(criteria);
        return ApiResponse.success(null);
    }

    @PostMapping("/callback")
    public ApiResponse<?> callback(@RequestBody PaymentV1Dto.Request.CallbackTransaction callback) {
        System.out.println("callback = " + callback);
        PaymentCriteria.Complete complete = PaymentCriteria.Complete.of(callback.transactionKey(), callback.orderId(), callback.cardType(), callback.cardNo(), callback.amount());
        paymentFacade.complete(complete);
        return ApiResponse.success();
    }

}
