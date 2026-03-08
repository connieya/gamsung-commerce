package com.loopers.application.order;

import com.loopers.domain.cart.CartItem;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderNoIssue;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.order.OrderService;
import com.loopers.infrastructure.feign.commerce.CommerceApiClient;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
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
    private final CommerceApiClient commerceApiClient;
    private final OrderService orderService;
    private final OrderNoIssuer orderNoIssuer;
    private final CartService cartService;

    @Transactional
    public CommerceApiDto.PaymentReadyResponse ready(String orderNo, String orderKey, OrderCriteria.Ready criteria) {
        Order order = getOrCreateOrder(orderNo, criteria.userId(), criteria.orderItems(), criteria.couponId());
        order.validatePay();

        CommerceApiDto.PaymentReadyRequest readyRequest = new CommerceApiDto.PaymentReadyRequest(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getFinalAmount(),
                criteria.paymentMethod(),
                criteria.payKind(),
                orderKey
        );

        return commerceApiClient.paymentReady(readyRequest).data();
    }

    @Transactional(readOnly = true)
    public OrderResult.GetDetail getOrderDetail(Long orderId) {
        OrderInfo orderDetail = orderService.getOrderDetail(orderId);
        return OrderResult.GetDetail.from(orderDetail);
    }

    @Transactional(readOnly = true)
    public OrderResult.List getOrders(String userId) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        List<OrderInfo> orders = orderService.getOrdersByUserId(user.id());
        return OrderResult.List.from(orders);
    }

    @Transactional
    public OrderResult.IssueOrderNo issueOrderNo(boolean isNewOrderForm) {
        OrderNoIssue issue = orderNoIssuer.issue(isNewOrderForm);
        return OrderResult.IssueOrderNo.from(issue);
    }

    @Transactional(readOnly = true)
    public OrderResult.OrderForm getOrderForm(String userId, List<Long> cartItemIds) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();

        List<CartItem> cartItems = (cartItemIds != null && !cartItemIds.isEmpty())
                ? cartService.getCartItemsByIds(cartItemIds, user.id())
                : cartService.getCartItems(user.id());

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();
        Map<Long, CommerceApiDto.ProductResponse> productMap = commerceApiClient
                .getProducts(new CommerceApiDto.ProductBulkRequest(productIds)).data().stream()
                .collect(Collectors.toMap(CommerceApiDto.ProductResponse::id, Function.identity()));

        List<OrderResult.OrderForm.CartItemInfo> cartItemInfos = cartItems.stream()
                .map(item -> {
                    CommerceApiDto.ProductResponse product = productMap.get(item.getProductId());
                    return OrderResult.OrderForm.CartItemInfo.builder()
                            .cartId(item.getId())
                            .productId(item.getProductId())
                            .productName(product != null ? product.name() : null)
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .imageUrl(product != null ? product.imageUrl() : null)
                            .build();
                })
                .toList();

        Long totalAmount = cartItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();

        return OrderResult.OrderForm.builder()
                .member(OrderResult.OrderForm.Member.builder()
                        .userId(user.userId())
                        .email(user.email())
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

        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();

        List<Long> productIds = orderItems.stream()
                .map(OrderCriteria.OrderItem::productId)
                .toList();
        Map<Long, CommerceApiDto.ProductResponse> productMap = commerceApiClient
                .getProducts(new CommerceApiDto.ProductBulkRequest(productIds)).data().stream()
                .collect(Collectors.toMap(CommerceApiDto.ProductResponse::id, Function.identity()));

        List<OrderCommand.OrderItem> orderCommandItems = orderItems.stream()
                .map(item -> {
                    CommerceApiDto.ProductResponse product = productMap.get(item.productId());
                    if (product == null) {
                        throw new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + item.productId());
                    }
                    return OrderCommand.OrderItem.builder()
                            .productId(product.id())
                            .price(product.price())
                            .quantity(item.quantity())
                            .build();
                })
                .toList();

        Long totalAmount = orderCommandItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();

        Long discountAmount = commerceApiClient.calculateDiscount(
                new CommerceApiDto.CouponDiscountRequest(user.id(), couponId, totalAmount)
        ).data().discountAmount();

        OrderCommand command = OrderCommand.of(user.id(), orderCommandItems, discountAmount);
        orderService.place(command, orderNo);
        return orderService.getOrderByOrderNumber(orderNo);
    }
}
