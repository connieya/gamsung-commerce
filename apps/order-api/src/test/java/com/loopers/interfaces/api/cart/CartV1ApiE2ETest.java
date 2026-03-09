package com.loopers.interfaces.api.cart;

import com.loopers.annotation.SprintE2ETest;
import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartItem;
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
class CartV1ApiE2ETest {

    private static final String BASE_URL = "/api/v1/cart";
    private static final String USER_ID = "testuser";
    private static final Long USER_DB_ID = 1L;

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
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void mockUser() {
        CommerceApiDto.UserResponse user = new CommerceApiDto.UserResponse(USER_DB_ID, USER_ID, "test@test.com");
        when(commerceApiClient.getUser(USER_ID)).thenReturn(ApiResponse.success(user));
    }

    private void mockGetProducts(Long productId, String name, Long price) {
        CommerceApiDto.ProductResponse product = new CommerceApiDto.ProductResponse(productId, name, price, "http://img.com/a.jpg");
        when(commerceApiClient.getProducts(any())).thenReturn(ApiResponse.success(List.of(product)));
    }

    private Cart persistCartWithItems() {
        Cart cart = Cart.create(USER_DB_ID);
        CartItem item1 = CartItem.create(10L, 2L, 5000L);
        CartItem item2 = CartItem.create(20L, 1L, 20000L);
        cart.addItem(item1);
        cart.addItem(item2);
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(cart));
        return cart;
    }

    @Nested
    @DisplayName("GET /api/v1/cart")
    class GetCart {

        @Test
        @DisplayName("아이템이 있으면 목록과 총 금액을 반환한다")
        void getCart_withItems() {
            // given
            mockUser();
            Cart cart = persistCartWithItems();

            ParameterizedTypeReference<ApiResponse<CartV1Dto.Response.CartDetail>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<CartV1Dto.Response.CartDetail>> response =
                    testRestTemplate.exchange(BASE_URL, HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).hasSize(2),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(30000L) // 5000*2 + 20000*1
            );
        }

        @Test
        @DisplayName("장바구니가 없으면 빈 장바구니를 반환한다")
        void getCart_empty() {
            // given
            mockUser();

            ParameterizedTypeReference<ApiResponse<CartV1Dto.Response.CartDetail>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<CartV1Dto.Response.CartDetail>> response =
                    testRestTemplate.exchange(BASE_URL, HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).isEmpty(),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(0L)
            );
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cart/items")
    class AddItem {

        @Test
        @DisplayName("새 상품을 추가하면 장바구니에 포함된다")
        void addItem_newProduct() {
            // given
            mockUser();
            mockGetProducts(10L, "상품A", 5000L);

            CartV1Dto.Request.AddItem request = new CartV1Dto.Request.AddItem(10L, 2L);

            ParameterizedTypeReference<ApiResponse<CartV1Dto.Response.CartDetail>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<CartV1Dto.Response.CartDetail>> response =
                    testRestTemplate.exchange(BASE_URL + "/items", HttpMethod.POST,
                            new HttpEntity<>(request, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).hasSize(1),
                    () -> assertThat(response.getBody().data().items().get(0).productId()).isEqualTo(10L),
                    () -> assertThat(response.getBody().data().items().get(0).quantity()).isEqualTo(2L)
            );
        }

        @Test
        @DisplayName("기존 상품을 추가하면 수량이 증가한다")
        void addItem_existingProduct_increaseQuantity() {
            // given
            mockUser();
            persistCartWithItems();
            mockGetProducts(10L, "상품A", 5000L);

            CartV1Dto.Request.AddItem request = new CartV1Dto.Request.AddItem(10L, 3L);

            ParameterizedTypeReference<ApiResponse<CartV1Dto.Response.CartDetail>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<CartV1Dto.Response.CartDetail>> response =
                    testRestTemplate.exchange(BASE_URL + "/items", HttpMethod.POST,
                            new HttpEntity<>(request, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).hasSize(2),
                    () -> assertThat(response.getBody().data().items().stream()
                            .filter(i -> i.productId().equals(10L))
                            .findFirst()
                            .orElseThrow()
                            .quantity()).isEqualTo(5L) // 2 + 3
            );
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/cart/items/{itemId}")
    class UpdateItemQuantity {

        @Test
        @DisplayName("수량 변경에 성공한다")
        void updateItemQuantity_success() {
            // given
            mockUser();
            Cart cart = persistCartWithItems();
            Long itemId = cart.getItems().get(0).getId();

            CartV1Dto.Request.UpdateQuantity request = new CartV1Dto.Request.UpdateQuantity(10L);

            ParameterizedTypeReference<ApiResponse<Void>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<Void>> response =
                    testRestTemplate.exchange(BASE_URL + "/items/" + itemId, HttpMethod.PUT,
                            new HttpEntity<>(request, createHeaders()), responseType);

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }

        @Test
        @DisplayName("없는 아이템이면 500 에러를 반환한다")
        void updateItemQuantity_notFound() {
            // given
            mockUser();
            CartV1Dto.Request.UpdateQuantity request = new CartV1Dto.Request.UpdateQuantity(10L);

            ParameterizedTypeReference<ApiResponse<Void>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<Void>> response =
                    testRestTemplate.exchange(BASE_URL + "/items/99999", HttpMethod.PUT,
                            new HttpEntity<>(request, createHeaders()), responseType);

            // then
            assertTrue(response.getStatusCode().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cart/items/{itemId}")
    class RemoveItem {

        @Test
        @DisplayName("아이템 삭제에 성공한다")
        void removeItem_success() {
            // given
            mockUser();
            Cart cart = persistCartWithItems();
            Long itemId = cart.getItems().get(0).getId();

            ParameterizedTypeReference<ApiResponse<Void>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<Void>> response =
                    testRestTemplate.exchange(BASE_URL + "/items/" + itemId, HttpMethod.DELETE,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cart")
    class ClearCart {

        @Test
        @DisplayName("장바구니 비우기에 성공한다")
        void clearCart_success() {
            // given
            mockUser();
            persistCartWithItems();

            ParameterizedTypeReference<ApiResponse<Void>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<Void>> response =
                    testRestTemplate.exchange(BASE_URL, HttpMethod.DELETE,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cart/count")
    class GetCartCount {

        @Test
        @DisplayName("총 수량을 반환한다")
        void getCartCount_withItems() {
            // given
            mockUser();
            persistCartWithItems();

            ParameterizedTypeReference<ApiResponse<CartV1Dto.Response.CartCount>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<CartV1Dto.Response.CartCount>> response =
                    testRestTemplate.exchange(BASE_URL + "/count", HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().count()).isEqualTo(3L) // 2 + 1
            );
        }

        @Test
        @DisplayName("장바구니가 없으면 0을 반환한다")
        void getCartCount_empty() {
            // given
            mockUser();

            ParameterizedTypeReference<ApiResponse<CartV1Dto.Response.CartCount>> responseType =
                    new ParameterizedTypeReference<>() {};

            // when
            ResponseEntity<ApiResponse<CartV1Dto.Response.CartCount>> response =
                    testRestTemplate.exchange(BASE_URL + "/count", HttpMethod.GET,
                            new HttpEntity<>(null, createHeaders()), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().count()).isEqualTo(0L)
            );
        }
    }
}
