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
        // 1. orderNo로 주문 조회 시도
        Order order = null;
        try {
            order = orderService.getOrderByOrderNumber(orderNo);
        } catch (Exception e) {
            // 주문이 없으면 생성
        }
        
        // 2. 주문이 없으면 생성
        if (order == null && criteria.orderItems() != null && !criteria.orderItems().isEmpty()) {
            // 주문번호 발급 여부만 확인 (ready 단계에서는 서명 검증 생략)
            // orderKey는 이미 order-no API에서 발급된 것이므로 사용 가능
            
            User user = userService.findByUserId(criteria.userId());
            List<Long> productIds = criteria.orderItems().stream()
                    .map(OrderV1Dto.OrderItem::getProductId)
                    .toList();
            List<Product> products = productService.findAllById(productIds);
            
            Long totalAmount = criteria.orderItems().stream()
                    .mapToLong(item -> {
                        Product product = products.stream()
                                .filter(p -> p.getId().equals(item.getProductId()))
                                .findFirst()
                                .orElseThrow();
                        return product.getPrice() * item.getQuantity();
                    })
                    .sum();
            
            Long discountAmount = couponService.calculateDiscountAmount(criteria.couponId(), totalAmount);
            
            List<OrderCommand.OrderItem> orderCommandItems = criteria.orderItems().stream()
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
            order = orderService.getOrderByOrderNumber(orderNo);
        }
        
        // 3. 주문이 없으면 에러
        if (order == null) {
            order = orderService.getOrderByOrderNumber(orderNo);
        }
        
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
        Order order = orderService.getOrderByOrderNumber(orderNo);
        order.validatePay();
        
        PaymentCommand.Transaction transactionCommand = PaymentCommand.Transaction.of(
                order.getId(),
                order.getOrderNumber(),
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
}
