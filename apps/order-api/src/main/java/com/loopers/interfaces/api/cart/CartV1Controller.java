package com.loopers.interfaces.api.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartService;
import com.loopers.infrastructure.feign.commerce.CommerceApiClient;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartV1Controller {

    private final CartService cartService;
    private final CommerceApiClient commerceApiClient;

    @GetMapping
    public ApiResponse<CartV1Dto.Response.CartDetail> getCart(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        Cart cart = cartService.getCart(user.id());
        return ApiResponse.success(CartV1Dto.Response.CartDetail.from(cart));
    }

    @PostMapping("/items")
    public ApiResponse<CartV1Dto.Response.CartDetail> addItem(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody CartV1Dto.Request.AddItem request
    ) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        Cart cart = cartService.addItem(user.id(), request.productId(), request.quantity());
        return ApiResponse.success(CartV1Dto.Response.CartDetail.from(cart));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<Void> updateItemQuantity(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable Long itemId,
            @RequestBody CartV1Dto.Request.UpdateQuantity request
    ) {
        cartService.updateItemQuantity(itemId, request.quantity());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> removeItem(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable Long itemId
    ) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        cartService.removeItem(user.id(), itemId);
        return ApiResponse.success(null);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        cartService.clearCart(user.id());
        return ApiResponse.success(null);
    }

    @GetMapping("/count")
    public ApiResponse<CartV1Dto.Response.CartCount> getCartCount(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        Long count = cartService.getCartCount(user.id());
        return ApiResponse.success(new CartV1Dto.Response.CartCount(count));
    }
}
