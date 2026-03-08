package com.loopers.interfaces.api.internal;

import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready")
    public ApiResponse<InternalDto.PaymentReadyResponse> ready(@RequestBody InternalDto.PaymentReadyRequest request) {
        PaymentCommand.Ready readyCommand = PaymentCommand.Ready.of(
                request.orderId(),
                request.orderNumber(),
                request.userId(),
                request.amount(),
                PaymentMethod.valueOf(request.paymentMethod()),
                PayKind.valueOf(request.payKind())
        );

        PaymentInfo.ReadyResult result = paymentService.ready(readyCommand, request.orderKey());
        return ApiResponse.success(new InternalDto.PaymentReadyResponse(result.paymentId(), result.paymentStatus().toString()));
    }
}
