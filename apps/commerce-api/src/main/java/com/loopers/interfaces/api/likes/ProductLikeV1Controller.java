package com.loopers.interfaces.api.likes;

import com.loopers.domain.likes.ProductLikeService;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like/products")
public class ProductLikeV1Controller implements ProductLikeV1ApiSpec {

    private final ProductLikeService productLikeService;

    @PostMapping("/{productId}")
    @Override
    public ApiResponse<?> add(@RequestHeader(ApiHeaders.USER_ID) String userId, @PathVariable("productId") Long productId) {
        return ApiResponse.success();
    }

    @DeleteMapping("/{productId")
    @Override
    public ApiResponse<?> remove(@RequestHeader(ApiHeaders.USER_ID) String userId, @PathVariable("productId") Long productId) {
        return ApiResponse.success();
    }

    @GetMapping
    @Override
    public ApiResponse<?> getMyLikes(@RequestHeader(ApiHeaders.USER_ID) String userId) {
        return null;
    }
}
