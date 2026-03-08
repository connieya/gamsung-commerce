package com.loopers.application.order;

import com.loopers.domain.cart.CartItem;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderNoIssue;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.infrastructure.feign.commerce.CommerceApiClient;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
import com.loopers.interfaces.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    OrderFacade orderFacade;

    @Mock
    CommerceApiClient commerceApiClient;

    @Mock
    OrderService orderService;

    @Mock
    OrderNoIssuer orderNoIssuer;

    @Mock
    CartService cartService;

    private static final String ORDER_NO = "ORD-20260308-0001";
    private static final String ORDER_KEY = "order-key-123";
    private static final String USER_ID = "testuser";

    private Order createTestOrder(String orderNo, Long discountAmount) {
        Order order = Order.create(
                OrderCommand.of(1L, List.of(
                        OrderCommand.OrderItem.builder()
                                .productId(1L)
                                .quantity(2L)
                                .price(10000L)
                                .build()
                ), discountAmount),
                orderNo
        );
        ReflectionTestUtils.setField(order, "id", 1L);
        return order;
    }

    private CommerceApiDto.UserResponse createTestUser() {
        return new CommerceApiDto.UserResponse(1L, USER_ID, "test@test.com");
    }

    private CartItem createTestCartItem(Long id, Long productId, Long quantity, Long price) {
        CartItem cartItem = CartItem.create(productId, quantity, price);
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }

    @Nested
    @DisplayName("결제 준비 (ready)")
    class ReadyTest {

        @Test
        @DisplayName("기존 주문이 있으면 결제 준비 요청을 보낸다")
        void ready_withExistingOrder() {
            // given
            Order order = createTestOrder(ORDER_NO, 0L);
            when(orderService.findOrderByOrderNumber(ORDER_NO)).thenReturn(Optional.of(order));

            CommerceApiDto.PaymentReadyResponse readyResponse = new CommerceApiDto.PaymentReadyResponse(1L, "READY");
            when(commerceApiClient.paymentReady(any())).thenReturn(ApiResponse.success(readyResponse));

            OrderCriteria.Ready criteria = new OrderCriteria.Ready("CARD", "NORMAL", USER_ID, null, null);

            // when
            CommerceApiDto.PaymentReadyResponse result = orderFacade.ready(ORDER_NO, ORDER_KEY, criteria);

            // then
            assertThat(result.paymentId()).isEqualTo(1L);
            assertThat(result.paymentStatus()).isEqualTo("READY");
            verify(commerceApiClient).paymentReady(any());
        }

        @Test
        @DisplayName("새 주문을 생성하고 결제 준비 요청을 보낸다")
        void ready_withNewOrder() {
            // given
            when(orderService.findOrderByOrderNumber(ORDER_NO)).thenReturn(Optional.empty());

            CommerceApiDto.UserResponse user = createTestUser();
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            CommerceApiDto.ProductResponse product = new CommerceApiDto.ProductResponse(1L, "상품A", 10000L, "http://img.com/a.jpg");
            when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product)));

            CommerceApiDto.CouponDiscountResponse discountResponse = new CommerceApiDto.CouponDiscountResponse(1000L);
            when(commerceApiClient.calculateDiscount(any())).thenReturn(ApiResponse.success(discountResponse));

            Order order = createTestOrder(ORDER_NO, 1000L);
            when(orderService.getOrderByOrderNumber(ORDER_NO)).thenReturn(order);

            CommerceApiDto.PaymentReadyResponse readyResponse = new CommerceApiDto.PaymentReadyResponse(1L, "READY");
            when(commerceApiClient.paymentReady(any())).thenReturn(ApiResponse.success(readyResponse));

            OrderCriteria.OrderItem orderItem = new OrderCriteria.OrderItem(1L, 2L);
            OrderCriteria.Ready criteria = new OrderCriteria.Ready("CARD", "NORMAL", USER_ID, List.of(orderItem), 1L);

            // when
            CommerceApiDto.PaymentReadyResponse result = orderFacade.ready(ORDER_NO, ORDER_KEY, criteria);

            // then
            assertThat(result.paymentId()).isEqualTo(1L);
            assertThat(result.paymentStatus()).isEqualTo("READY");
            verify(orderService).place(any(OrderCommand.class), any(String.class));
        }
    }

    @Nested
    @DisplayName("주문 상세 조회 (getOrderDetail)")
    class GetOrderDetailTest {

        @Test
        @DisplayName("주문 상세 정보를 반환한다")
        void getOrderDetail_success() {
            // given
            OrderInfo orderInfo = OrderInfo.builder()
                    .orderId(1L)
                    .orderNumber(ORDER_NO)
                    .totalAmount(20000L)
                    .discountAmount(1000L)
                    .orderStatus(OrderStatus.INIT)
                    .createdAt(ZonedDateTime.now())
                    .orderItems(List.of(
                            OrderInfo.OrderItem.builder()
                                    .productId(1L)
                                    .quantity(2L)
                                    .price(10000L)
                                    .build()
                    ))
                    .build();
            when(orderService.getOrderDetail(1L)).thenReturn(orderInfo);

            // when
            OrderResult.GetDetail result = orderFacade.getOrderDetail(1L);

            // then
            assertThat(result.getOrderId()).isEqualTo(1L);
            assertThat(result.getOrderNumber()).isEqualTo(ORDER_NO);
            assertThat(result.getTotalAmount()).isEqualTo(20000L);
            assertThat(result.getDiscountAmount()).isEqualTo(1000L);
            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.INIT);
            assertThat(result.getOrderItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("주문 목록 조회 (getOrders)")
    class GetOrdersTest {

        @Test
        @DisplayName("사용자의 주문 목록을 반환한다")
        void getOrders_success() {
            // given
            CommerceApiDto.UserResponse user = createTestUser();
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            OrderInfo orderInfo = OrderInfo.builder()
                    .orderId(1L)
                    .orderNumber(ORDER_NO)
                    .totalAmount(20000L)
                    .discountAmount(0L)
                    .orderStatus(OrderStatus.PAID)
                    .createdAt(ZonedDateTime.now())
                    .orderItems(List.of())
                    .build();
            when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(orderInfo));

            // when
            OrderResult.List result = orderFacade.getOrders(USER_ID);

            // then
            assertThat(result.getOrders()).hasSize(1);
            assertThat(result.getOrders().get(0).getOrderNumber()).isEqualTo(ORDER_NO);
        }

        @Test
        @DisplayName("주문이 없으면 빈 목록을 반환한다")
        void getOrders_returnsEmpty() {
            // given
            CommerceApiDto.UserResponse user = createTestUser();
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));
            when(orderService.getOrdersByUserId(1L)).thenReturn(Collections.emptyList());

            // when
            OrderResult.List result = orderFacade.getOrders(USER_ID);

            // then
            assertThat(result.getOrders()).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문번호 발급 (issueOrderNo)")
    class IssueOrderNoTest {

        @Test
        @DisplayName("주문번호를 발급하고 결과를 반환한다")
        void issueOrderNo_success() {
            // given
            OrderNoIssue issue = new OrderNoIssue(
                    "ORD-20260308-0002",
                    "signature-abc",
                    1709856000L,
                    "verify-key-123",
                    "order-key-456"
            );
            when(orderNoIssuer.issue(true)).thenReturn(issue);

            // when
            OrderResult.IssueOrderNo result = orderFacade.issueOrderNo(true);

            // then
            assertThat(result.getOrderNo()).isEqualTo("ORD-20260308-0002");
            assertThat(result.getOrderSignature()).isEqualTo("signature-abc");
            assertThat(result.getTimestamp()).isEqualTo(1709856000L);
            assertThat(result.getOrderVerifyKey()).isEqualTo("verify-key-123");
            assertThat(result.getOrderKey()).isEqualTo("order-key-456");
        }
    }

    @Nested
    @DisplayName("주문 양식 조회 (getOrderForm)")
    class GetOrderFormTest {

        @Test
        @DisplayName("cartItemIds 지정 시 해당 아이템으로 주문 양식을 반환한다")
        void getOrderForm_withCartItemIds() {
            // given
            CommerceApiDto.UserResponse user = createTestUser();
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            CartItem cartItem1 = createTestCartItem(1L, 10L, 2L, 10000L);
            CartItem cartItem2 = createTestCartItem(2L, 20L, 1L, 20000L);
            List<Long> cartItemIds = List.of(1L, 2L);
            when(cartService.getCartItemsByIds(cartItemIds, 1L)).thenReturn(List.of(cartItem1, cartItem2));

            CommerceApiDto.ProductResponse product1 = new CommerceApiDto.ProductResponse(10L, "상품A", 10000L, "http://img.com/a.jpg");
            CommerceApiDto.ProductResponse product2 = new CommerceApiDto.ProductResponse(20L, "상품B", 20000L, "http://img.com/b.jpg");
            when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product1, product2)));

            // when
            OrderResult.OrderForm result = orderFacade.getOrderForm(USER_ID, cartItemIds);

            // then
            assertThat(result.getMember().getUserId()).isEqualTo(USER_ID);
            assertThat(result.getMember().getEmail()).isEqualTo("test@test.com");
            assertThat(result.getCartItems()).hasSize(2);
            assertThat(result.getCartItems().get(0).getProductName()).isEqualTo("상품A");
            assertThat(result.getCartItems().get(1).getProductName()).isEqualTo("상품B");
            assertThat(result.getTotalAmount()).isEqualTo(40000L); // 10000*2 + 20000*1
        }

        @Test
        @DisplayName("cartItemIds 미지정 시 전체 장바구니 아이템으로 주문 양식을 반환한다")
        void getOrderForm_withoutCartItemIds() {
            // given
            CommerceApiDto.UserResponse user = createTestUser();
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            CartItem cartItem = createTestCartItem(1L, 10L, 3L, 5000L);
            when(cartService.getCartItems(1L)).thenReturn(List.of(cartItem));

            CommerceApiDto.ProductResponse product = new CommerceApiDto.ProductResponse(10L, "상품C", 5000L, "http://img.com/c.jpg");
            when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product)));

            // when
            OrderResult.OrderForm result = orderFacade.getOrderForm(USER_ID, null);

            // then
            assertThat(result.getMember().getUserId()).isEqualTo(USER_ID);
            assertThat(result.getCartItems()).hasSize(1);
            assertThat(result.getCartItems().get(0).getProductName()).isEqualTo("상품C");
            assertThat(result.getCartItems().get(0).getQuantity()).isEqualTo(3L);
            assertThat(result.getTotalAmount()).isEqualTo(15000L); // 5000*3
        }
    }
}
