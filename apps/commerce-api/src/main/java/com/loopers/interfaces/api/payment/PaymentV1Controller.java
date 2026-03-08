package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
                request.payKind(),
                request.cardType(),
                request.cardNumber(),
                request.couponId()
        );
        paymentFacade.pay(criteria);
        return ApiResponse.success(null);
    }

    @PostMapping("/session")
    public ApiResponse<PaymentV1Dto.Response.PaymentSession> paymentSession(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody PaymentV1Dto.Request.PaymentSession request
    ) {
        PaymentCriteria.PaymentSession criteria = new PaymentCriteria.PaymentSession(
                request.paymentMethod(),
                request.payKind(),
                userId,
                request.orderItems().stream()
                        .map(item -> new PaymentCriteria.OrderItem(item.productId(), item.quantity()))
                        .toList(),
                request.cardType(),
                request.cardNumber(),
                request.couponId()
        );
        PaymentInfo.SessionResult result = paymentFacade.createPaymentSession(
                request.orderNo(),
                request.orderKey(),
                criteria
        );
        return ApiResponse.success(PaymentV1Dto.Response.PaymentSession.from(result));
    }

    @PostMapping("/callback")
    public ApiResponse<?> callback(@RequestBody PaymentV1Dto.Request.CallbackTransaction callback) {
        log.debug("[결제콜백] transactionKey={}, orderId={}", callback.transactionKey(), callback.orderId());
        PaymentCriteria.Complete complete = PaymentCriteria.Complete.of(callback.transactionKey(), callback.orderId(), callback.cardType(), callback.cardNo(), callback.amount());
        paymentFacade.complete(complete);
        return ApiResponse.success();
    }

}
