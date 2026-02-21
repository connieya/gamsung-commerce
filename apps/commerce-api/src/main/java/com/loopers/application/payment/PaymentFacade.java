package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.domain.order.OrderService;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.coupon.CouponService;
import com.loopers.interfaces.api.order.OrderV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

    public void pay(PaymentCriteria.Pay criteria) {
        Order order = orderService.getOrder(criteria.orderId());
        order.validatePay();

        paymentService.ready(PaymentCommand.Ready.of(order.getId(), order.getOrderNumber(), order.getUserId(), order.getFinalAmount(), criteria.paymentMethod()), null);

        PaymentProcessor paymentProcessor = paymentProcessorMap.get(criteria.paymentMethod().toString());
        paymentProcessor.pay(PaymentProcessContext.of(criteria));
    }
    
    @Transactional
    public PaymentService.PaymentReadyResult ready(String orderNo, String orderKey, PaymentCriteria.Ready criteria) {
        Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());
        order.validatePay();

        PaymentCommand.Ready readyCommand = PaymentCommand.Ready.of(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getFinalAmount(),
                criteria.paymentMethod()
        );

        return paymentService.ready(readyCommand, orderKey);
    }
    
    @Transactional
    public PaymentService.PaymentSessionResult createPaymentSession(String orderNo, String orderKey, PaymentCriteria.PaymentSession criteria) {
        Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());
        order.validatePay();

        paymentService.ensurePendingPayment(
                PaymentCommand.Ready.of(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getUserId(),
                        order.getFinalAmount(),
                        criteria.paymentMethod()
                )
        );

        PaymentCommand.Transaction transactionCommand = PaymentCommand.Transaction.of(
                order.getId(),
                order.getOrderNumber(),
                criteria.paymentMethod(),
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
            List<OrderV1Dto.OrderItem> orderItems,
            Long couponId
    ) {
        Order order = null;
        try {
            order = orderService.getOrderByOrderNumber(orderNo);
        } catch (Exception e) {
            // 주문이 없는 경우 새로 생성한다.
        }

        if (order == null && orderItems != null && !orderItems.isEmpty()) {
            User user = userService.findByUserId(userId);
            List<Long> productIds = orderItems.stream()
                    .map(OrderV1Dto.OrderItem::getProductId)
                    .toList();
            List<Product> products = productService.findAllById(productIds);

            Long totalAmount = orderItems.stream()
                    .mapToLong(item -> {
                        Product product = products.stream()
                                .filter(p -> p.getId().equals(item.getProductId()))
                                .findFirst()
                                .orElseThrow();
                        return product.getPrice() * item.getQuantity();
                    })
                    .sum();

            Long discountAmount = couponService.calculateDiscountAmount(couponId, totalAmount);

            List<OrderCommand.OrderItem> orderCommandItems = orderItems.stream()
                    .map(item -> {
                        Product product = products.stream()
                                .filter(p -> p.getId().equals(item.getProductId()))
                                .findFirst()
                                .orElseThrow();
                        return OrderCommand.OrderItem.builder()
                                .productId(product.getId())
                                .price(product.getPrice())
                                .quantity(item.getQuantity())
                                .build();
                    })
                    .toList();

            OrderCommand command = OrderCommand.of(user.getId(), orderCommandItems, discountAmount);
            orderService.place(command, orderNo);
            return orderService.getOrderByOrderNumber(orderNo);
        }

        if (order == null) {
            return orderService.getOrderByOrderNumber(orderNo);
        }

        return order;
    }
}
