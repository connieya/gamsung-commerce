package com.loopers.application.order;

import com.loopers.domain.cart.CartItem;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderNoIssue;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
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
public class OrderFacade {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OrderNoIssuer orderNoIssuer;
    private final CartService cartService;
    private final CouponService couponService;

    @Transactional
    public PaymentInfo.ReadyResult ready(String orderNo, String orderKey, OrderCriteria.Ready criteria) {
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

    @Transactional(readOnly = true)
    public OrderResult.GetDetail getOrderDetail(Long orderId){
        OrderInfo orderDetail = orderService.getOrderDetail(orderId);

        return OrderResult.GetDetail.from(orderDetail);
    }

    @Transactional(readOnly = true)
    public OrderResult.List getOrders(String userId) {
        User user = userService.findByUserId(userId);
        List<OrderInfo> orders = orderService.getOrdersByUserId(user.getId());
        return OrderResult.List.from(orders);
    }

    @Transactional
    public OrderResult.IssueOrderNo issueOrderNo(boolean isNewOrderForm) {
        OrderNoIssue issue = orderNoIssuer.issue(isNewOrderForm);
        return OrderResult.IssueOrderNo.from(issue);
    }

    @Transactional(readOnly = true)
    public OrderResult.OrderForm getOrderForm(String userId, List<Long> cartItemIds) {
        User user = userService.findByUserId(userId);

        List<CartItem> cartItems = (cartItemIds != null && !cartItemIds.isEmpty())
                ? cartService.getCartItemsByIds(cartItemIds, user.getId())
                : cartService.getCartItems(user.getId());

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();
        Map<Long, Product> productMap = productService.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderResult.OrderForm.CartItemInfo> cartItemInfos = cartItems.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    return OrderResult.OrderForm.CartItemInfo.builder()
                            .cartId(item.getId())
                            .productId(item.getProductId())
                            .productName(product != null ? product.getName() : null)
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .imageUrl(product != null ? product.getImageUrl() : null)
                            .build();
                })
                .toList();

        Long totalAmount = cartItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();

        return OrderResult.OrderForm.builder()
                .member(OrderResult.OrderForm.Member.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .build())
                .cartItems(cartItemInfos)
                .totalAmount(totalAmount)
                .build();
    }

    private Order getOrCreateOrder(
            String orderNo,
            String userId,
            List<OrderCriteria.OrderItem> orderItems,
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
                .map(OrderCriteria.OrderItem::productId)
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

        Long discountAmount = couponService.calculateDiscountAmount(user.getId(), couponId, totalAmount);

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
