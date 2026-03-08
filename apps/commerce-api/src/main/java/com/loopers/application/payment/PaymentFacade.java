package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderApiClient orderApiClient;
    private final Map<String, PaymentProcessor> paymentProcessorMap;
    private final PaymentService paymentService;

    @Transactional
    public void pay(PaymentCriteria.Pay criteria) {
        OrderApiDto.OrderResponse order = orderApiClient.getOrder(criteria.orderId()).data();
        validatePay(order);

        PayKind resolvedPayKind = resolvePayKind(criteria.paymentMethod(), criteria.payKind());
        paymentService.ready(PaymentCommand.Ready.of(order.orderId(), order.orderNumber(), order.userId(), order.finalAmount(), criteria.paymentMethod(), resolvedPayKind), null);

        PaymentProcessor paymentProcessor = paymentProcessorMap.get(criteria.paymentMethod().toString());
        paymentProcessor.pay(PaymentProcessContext.of(criteria));
    }

    @Transactional
    public PaymentInfo.SessionResult createPaymentSession(String orderNo, String orderKey, PaymentCriteria.PaymentSession criteria) {
        OrderApiDto.OrderResponse order = orderApiClient.getOrderByOrderNumber(orderNo).data();
        validatePay(order);

        PayKind resolvedPayKind = resolvePayKind(criteria.paymentMethod(), criteria.payKind());
        paymentService.ensurePendingPayment(
                PaymentCommand.Ready.of(
                        order.orderId(),
                        order.orderNumber(),
                        order.userId(),
                        order.finalAmount(),
                        criteria.paymentMethod(),
                        resolvedPayKind
                )
        );

        PaymentCommand.Transaction transactionCommand = PaymentCommand.Transaction.of(
                order.orderId(),
                order.orderNumber(),
                criteria.paymentMethod(),
                resolvedPayKind,
                criteria.cardType(),
                criteria.cardNumber(),
                order.finalAmount(),
                order.userId(),
                criteria.couponId()
        );

        return paymentService.createPaymentSession(transactionCommand, orderKey);
    }

    @Transactional
    public void complete(PaymentCriteria.Complete complete) {
        PaymentCommand.Search search = PaymentCommand.Search.of(complete.transactionKey(), complete.orderNumber());
        paymentService.complete(search);
    }

    private void validatePay(OrderApiDto.OrderResponse order) {
        if (!"INIT".equals(order.orderStatus())) {
            throw new PaymentException.InvalidPayKindException(ErrorType.ORDER_INVALID_STATUS);
        }
        if (order.finalAmount() <= 0) {
            throw new PaymentException.InvalidPayKindException(ErrorType.ORDER_INVALID_AMOUNT);
        }
    }

    private PayKind resolvePayKind(PaymentMethod paymentMethod, PayKind payKind) {
        if (payKind != null) {
            return payKind;
        }

        return switch (paymentMethod) {
            case CARD -> PayKind.CARD;
            case POINT -> PayKind.POINT;
            case ACCOUNT -> PayKind.ACCOUNT_TRANSFER;
            case SIMPLE_PAY -> throw new PaymentException.InvalidPayKindException(ErrorType.PAYMENT_INVALID_PAY_KIND);
        };
    }
}
