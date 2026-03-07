package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.domain.order.OrderService;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.order.Order;
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

    private final OrderService orderService;
    private final Map<String, PaymentProcessor> paymentProcessorMap;
    private final PaymentService paymentService;

    @Transactional
    public void pay(PaymentCriteria.Pay criteria) {
        Order order = orderService.getOrder(criteria.orderId());
        order.validatePay();

        PayKind resolvedPayKind = resolvePayKind(criteria.paymentMethod(), criteria.payKind());
        paymentService.ready(PaymentCommand.Ready.of(order.getId(), order.getOrderNumber(), order.getUserId(), order.getFinalAmount(), criteria.paymentMethod(), resolvedPayKind), null);

        PaymentProcessor paymentProcessor = paymentProcessorMap.get(criteria.paymentMethod().toString());
        paymentProcessor.pay(PaymentProcessContext.of(criteria));
    }

    @Transactional
    public PaymentInfo.SessionResult createPaymentSession(String orderNo, String orderKey, PaymentCriteria.PaymentSession criteria) {
        Order order = orderService.getOrderByOrderNumber(orderNo);
        order.validatePay();

        PayKind resolvedPayKind = resolvePayKind(criteria.paymentMethod(), criteria.payKind());
        paymentService.ensurePendingPayment(
                PaymentCommand.Ready.of(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getUserId(),
                        order.getFinalAmount(),
                        criteria.paymentMethod(),
                        resolvedPayKind
                )
        );

        PaymentCommand.Transaction transactionCommand = PaymentCommand.Transaction.of(
                order.getId(),
                order.getOrderNumber(),
                criteria.paymentMethod(),
                resolvedPayKind,
                criteria.cardType(),
                criteria.cardNumber(),
                order.getFinalAmount(),
                order.getUserId(),
                criteria.couponId()
        );

        return paymentService.createPaymentSession(transactionCommand, orderKey);
    }

    @Transactional
    public void complete(PaymentCriteria.Complete complete) {
        PaymentCommand.Search search = PaymentCommand.Search.of(complete.transactionKey(), complete.orderNumber());
        paymentService.complete(search);
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
