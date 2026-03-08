package com.loopers.interfaces.api.order;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartItem;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderStatus;
import com.loopers.infrastructure.feign.commerce.CommerceApiClient;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@SprintE2ETest
class OrderV1ApiE2ETest {

    private static final String BASE_URL = "/api/v1/orders";
    private static final String USER_ID = "testuser";

    private final DatabaseCleanUp databaseCleanUp;
    private final TestRestTemplate testRestTemplate;
    private final TestEntityManager testEntityManager;
    private final TransactionTemplate transactionTemplate;

    @MockitoBean
    private CommerceApiClient commerceApiClient;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.USER_ID, USER_ID);
        return headers;
    }

    private Order persistOrder(Long userId, String orderNo, Long productId, Long quantity, Long price, Long discountAmount) {
        OrderCommand command = OrderCommand.of(userId, List.of(
                OrderCommand.OrderItem.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .price(price)
                        .build()
        ), discountAmount);
        Order order = Order.create(command, orderNo);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(order));
        return order;
    }

    @Nested
    @DisplayName("POST /api/v1/orders/order-no")
    class IssueOrderNo {

        @Test
        @DisplayName("주문번호 발급에 성공하면 주문번호 정보를 반환한다")
        void issueOrderNo_success() {
            // given
            OrderV1Dto.Request.IssueOrderNo request = new OrderV1Dto.Request.IssueOrderNo(true);
            HttpEntity<OrderV1Dto.Request.IssueOrderNo> httpEntity = new HttpEntity<>(request, createHeaders());

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.IssueOrderNo>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.IssueOrderNo>> response =
                    testRestTemplate.exchange(BASE_URL + "/order-no", HttpMethod.POST, httpEntity, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().orderNo()).isNotBlank(),
                    () -> assertThat(response.getBody().data().orderSignature()).isNotBlank(),
                    () -> assertThat(response.getBody().data().orderKey()).isNotBlank(),
                    () -> assertThat(response.getBody().data().orderVerifyKey()).isNotBlank(),
                    () -> assertThat(response.getBody().data().timestamp()).isGreaterThan(0)
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}")
    class GetOrderDetail {

        @Test
        @DisplayName("주문 상세 조회에 성공하면 주문 정보를 반환한다")
        void getOrderDetail_success() {
            // given
            Order order = persistOrder(1L, "ORD-E2E-001", 10L, 2L, 5000L, 0L);

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Detail>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.Detail>> response =
                    testRestTemplate.exchange(BASE_URL + "/" + order.getId(), HttpMethod.GET,
                            new HttpEntity<>(null), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().orderNumber()).isEqualTo("ORD-E2E-001"),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(10000L),
                    () -> assertThat(response.getBody().data().discountAmount()).isEqualTo(0L),
                    () -> assertThat(response.getBody().data().orderStatus()).isEqualTo(OrderStatus.INIT),
                    () -> assertThat(response.getBody().data().items()).hasSize(1)
            );
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회하면 404를 반환한다")
        void getOrderDetail_notFound() {
            // given
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Detail>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.Detail>> response =
                    testRestTemplate.exchange(BASE_URL + "/99999", HttpMethod.GET,
                            new HttpEntity<>(null), responseType);

            // then
            assertTrue(response.getStatusCode().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class GetOrders {

        @Test
        @DisplayName("사용자의 주문 목록을 반환한다")
        void getOrders_success() {
            // given
            CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(1L, USER_ID, "test@test.com");
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            persistOrder(1L, "ORD-E2E-010", 10L, 2L, 5000L, 0L);
            persistOrder(1L, "ORD-E2E-011", 20L, 1L, 20000L, 1000L);

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.List>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.List>> response =
                    testRestTemplate.exchange(BASE_URL, HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().orders()).hasSize(2)
            );
        }

        @Test
        @DisplayName("주문이 없으면 빈 목록을 반환한다")
        void getOrders_empty() {
            // given
            CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(1L, USER_ID, "test@test.com");
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.List>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.List>> response =
                    testRestTemplate.exchange(BASE_URL, HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().orders()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/order-form")
    class GetOrderForm {

        @Test
        @DisplayName("전체 장바구니 아이템으로 주문 양식을 반환한다")
        void getOrderForm_allCartItems() {
            // given
            CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(1L, USER_ID, "test@test.com");
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            Cart cart = Cart.create(1L);
            CartItem item1 = CartItem.create(10L, 2L, 5000L);
            CartItem item2 = CartItem.create(20L, 1L, 20000L);
            cart.addItem(item1);
            cart.addItem(item2);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(cart));

            CommerceApiDto.ProductResponse product1 = new CommerceApiDto.ProductResponse(10L, "상품A", 5000L, "http://img.com/a.jpg");
            CommerceApiDto.ProductResponse product2 = new CommerceApiDto.ProductResponse(20L, "상품B", 20000L, "http://img.com/b.jpg");
            when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product1, product2)));

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.OrderForm>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.OrderForm>> response =
                    testRestTemplate.exchange(BASE_URL + "/order-form", HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().member().userId()).isEqualTo(USER_ID),
                    () -> assertThat(response.getBody().data().member().email()).isEqualTo("test@test.com"),
                    () -> assertThat(response.getBody().data().cartItems()).hasSize(2),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(30000L) // 5000*2 + 20000*1
            );
        }

        @Test
        @DisplayName("cartItemIds 지정 시 해당 아이템으로 주문 양식을 반환한다")
        void getOrderForm_selectedCartItems() {
            // given
            CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(1L, USER_ID, "test@test.com");
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            Cart cart = Cart.create(1L);
            CartItem item1 = CartItem.create(10L, 2L, 5000L);
            CartItem item2 = CartItem.create(20L, 1L, 20000L);
            cart.addItem(item1);
            cart.addItem(item2);
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(cart));

            CommerceApiDto.ProductResponse product1 = new CommerceApiDto.ProductResponse(10L, "상품A", 5000L, "http://img.com/a.jpg");
            when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product1)));

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.OrderForm>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.OrderForm>> response =
                    testRestTemplate.exchange(
                            BASE_URL + "/order-form?cartItemIds=" + item1.getId(),
                            HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().cartItems()).hasSize(1),
                    () -> assertThat(response.getBody().data().cartItems().get(0).productName()).isEqualTo("상품A"),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(10000L) // 5000*2
            );
        }
    }

    @Nested
    @DisplayName("POST /api/v1/orders/{orderNo}/ready")
    class ReadyOrder {

        @Test
        @DisplayName("기존 주문이 있으면 결제 준비에 성공한다")
        void ready_withExistingOrder() {
            // given
            Order order = persistOrder(1L, "ORD-E2E-020", 10L, 2L, 5000L, 0L);

            CommerceApiDto.PaymentReadyResponse readyResponse = new CommerceApiDto.PaymentReadyResponse(1L, "READY");
            when(commerceApiClient.paymentReady(any())).thenReturn(ApiResponse.success(readyResponse));

            OrderV1Dto.Request.Ready request = new OrderV1Dto.Request.Ready(
                    "CARD", "NORMAL", "order-key-123", List.of(), null
            );

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Ready>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.Ready>> response =
                    testRestTemplate.exchange(BASE_URL + "/ORD-E2E-020/ready", HttpMethod.POST,
                            new HttpEntity<>(request, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().paymentId()).isEqualTo(1L),
                    () -> assertThat(response.getBody().data().paymentStatus()).isEqualTo("READY")
            );
        }

        @Test
        @DisplayName("새 주문을 생성하고 결제 준비에 성공한다")
        void ready_withNewOrder() {
            // given
            CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(1L, USER_ID, "test@test.com");
            when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));

            CommerceApiDto.ProductResponse product = new CommerceApiDto.ProductResponse(10L, "상품A", 5000L, "http://img.com/a.jpg");
            when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product)));

            CommerceApiDto.CouponDiscountResponse discountResponse = new CommerceApiDto.CouponDiscountResponse(0L);
            when(commerceApiClient.calculateDiscount(any())).thenReturn(ApiResponse.success(discountResponse));

            CommerceApiDto.PaymentReadyResponse readyResponse = new CommerceApiDto.PaymentReadyResponse(1L, "READY");
            when(commerceApiClient.paymentReady(any())).thenReturn(ApiResponse.success(readyResponse));

            OrderV1Dto.Request.Ready request = new OrderV1Dto.Request.Ready(
                    "CARD", "NORMAL", "order-key-456",
                    List.of(OrderV1Dto.OrderItem.builder().productId(10L).quantity(2L).build()),
                    null
            );

            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Ready>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<OrderV1Dto.Response.Ready>> response =
                    testRestTemplate.exchange(BASE_URL + "/ORD-NEW-001/ready", HttpMethod.POST,
                            new HttpEntity<>(request, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().paymentId()).isEqualTo(1L),
                    () -> assertThat(response.getBody().data().paymentStatus()).isEqualTo("READY")
            );
        }
    }
}
