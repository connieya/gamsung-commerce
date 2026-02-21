package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.domain.order.OrderService;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final Map<String, PaymentProcessor> paymentProcessorMap;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final UserService userService;
    private final CouponService couponService;
    private final OrderNoIssuer orderNoIssuer;

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
    public PaymentService.PaymentReadyResult ready(String orderNo, String orderKey, PaymentCriteria.Ready criteria) {
        Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());
        order.validatePay();

        PayKind resolvedPayKind = resolvePayKind(criteria.paymentMethod(), criteria.payKind());
        PaymentCommand.Ready readyCommand = PaymentCommand.Ready.of(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getFinalAmount(),
                criteria.paymentMethod(),
                resolvedPayKind
        );

        return paymentService.ready(readyCommand, orderKey);
    }
    
    @Transactional
    public PaymentService.PaymentSessionResult createPaymentSession(String orderNo, String orderKey, PaymentCriteria.PaymentSession criteria) {
        Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());
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

    private Order getOrCreateOrder(
            String orderNo,
            String userId,
            List<PaymentCriteria.OrderItem> orderItems,
            Long couponId
    ) {
        Optional<Order> existingOrder = orderService.findOrderByOrderNumber(orderNo);
        if (existingOrder.isPresent()) {
            return existingOrder.get();
        }

        if (orderItems == null || orderItems.isEmpty()) {
            return orderService.getOrderByOrderNumber(orderNo);
        }

        User user = userService.findByUserId(userId);
        List<Long> productIds = orderItems.stream()
                .map(PaymentCriteria.OrderItem::productId)
                .toList();
        Map<Long, Product> productMap = productService.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderCommand.OrderItem> orderCommandItems = orderItems.stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());
                    if (product == null) {
                        throw new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + item.productId());
                    }
                    return OrderCommand.OrderItem.builder()
                            .productId(product.getId())
                            .price(product.getPrice())
                            .quantity(item.quantity())
                            .build();
                })
                .toList();

        Long totalAmount = orderCommandItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();

        Long discountAmount = couponService.calculateDiscountAmount(couponId, totalAmount);

        OrderCommand command = OrderCommand.of(user.getId(), orderCommandItems, discountAmount);
        orderService.place(command, orderNo);
        return orderService.getOrderByOrderNumber(orderNo);
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
