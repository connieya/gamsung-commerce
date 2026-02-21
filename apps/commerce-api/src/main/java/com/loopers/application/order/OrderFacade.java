package com.loopers.application.order;

import com.loopers.domain.cart.CartItem;
import com.loopers.domain.cart.CartRepository;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderNoIssue;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  OrderFacade {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final OrderNoIssuer orderNoIssuer;
    private final CartRepository cartRepository;

    @Transactional
    public OrderResult.Create place(OrderCriteria orderCriteria) {
        // 주문번호 서명 검증
        orderNoIssuer.verify(orderCriteria.getOrderNo(), orderCriteria.getOrderSignature(), orderCriteria.getOrderKey());

        User user = userService.findByUserId(orderCriteria.getUserId());

        List<Product> products = productService.findAllById(orderCriteria.getProductIds());

        Long discountAmount = couponService.calculateDiscountAmount(orderCriteria.getCouponId(), orderCriteria.getTotalAmount(products));

        OrderCommand command = OrderCommandMapper.map(user.getId(), orderCriteria, products, discountAmount);
        return OrderResult.Create.from(orderService.place(command, orderCriteria.getOrderNo()));
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
                ? cartRepository.findItemsByIdsAndUserId(cartItemIds, user.getId())
                : cartRepository.findItemsByUserId(user.getId());

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
}


