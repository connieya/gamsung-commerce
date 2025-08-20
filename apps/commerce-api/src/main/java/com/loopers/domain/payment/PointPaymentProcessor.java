package com.loopers.domain.payment;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("POINT")
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final PointService pointService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public Payment pay(PaymentCommand paymentCommand) {
        pointService.deduct(paymentCommand.getUserId(), paymentCommand.getFinalAmount());
        Payment payment = paymentService.create(paymentCommand);
        orderService.complete(paymentCommand.getOrderId());
        return payment;
    }
}
