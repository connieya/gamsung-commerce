package com.loopers.interfaces.api.likes;

import com.loopers.application.likes.GetLikeProductResult;
import com.loopers.application.likes.GetLikeProductUseCase;
import com.loopers.domain.likes.ProductLikeService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like/products")
public class ProductLikeV1Controller implements ProductLikeV1ApiSpec {

    private final ProductLikeService productLikeService;
    private final UserService userService;
    private final GetLikeProductUseCase getLikeProductUseCase;

    @PostMapping("/{productId}")
    @Override
    public ApiResponse<Void> add(@RequestHeader(ApiHeaders.USER_ID) String userId, @PathVariable("productId") Long productId) {
        User user = userService.findByUserId(userId);
        productLikeService.add(user.getId(), productId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Void> remove(@RequestHeader(ApiHeaders.USER_ID) String userId, @PathVariable("productId") Long productId) {
        User user = userService.findByUserId(userId);
        productLikeService.remove(user.getId(), productId);
        return ApiResponse.success(null);
    }

    @GetMapping
    @Override
    public ApiResponse<ProductLikeV1Dto.LikedProductResponse> getMyLikes(@RequestHeader(ApiHeaders.USER_ID) String userId) {
        GetLikeProductResult likedProducts = getLikeProductUseCase.getLikedProducts(userId);
        return ApiResponse.success(ProductLikeV1Dto.LikedProductResponse.from(likedProducts));
    }
}
