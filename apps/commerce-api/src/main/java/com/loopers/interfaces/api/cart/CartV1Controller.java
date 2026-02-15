package com.loopers.interfaces.api.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartV1Controller {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<CartV1Dto.Response.CartDetail> getCart(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        User user = userService.findByUserId(userId);
        Cart cart = cartService.getCart(user.getId());
        return ApiResponse.success(CartV1Dto.Response.CartDetail.from(cart));
    }

    @PostMapping("/items")
    public ApiResponse<CartV1Dto.Response.CartDetail> addItem(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody CartV1Dto.Request.AddItem request
    ) {
        User user = userService.findByUserId(userId);
        Cart cart = cartService.addItem(user.getId(), request.productId(), request.quantity());
        return ApiResponse.success(CartV1Dto.Response.CartDetail.from(cart));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<Void> updateItemQuantity(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable Long itemId,
            @RequestBody CartV1Dto.Request.UpdateQuantity request
    ) {
        User user = userService.findByUserId(userId);
        cartService.updateItemQuantity(itemId, request.quantity());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> removeItem(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable Long itemId
    ) {
        User user = userService.findByUserId(userId);
        cartService.removeItem(user.getId(), itemId);
        return ApiResponse.success(null);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    ) {
        User user = userService.findByUserId(userId);
        cartService.clearCart(user.getId());
        return ApiResponse.success(null);
    }
}
