package com.loopers.interfaces.api.likes;

import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeV1Controller implements LikeV1ApiSpec {

    @PostMapping("/products/{productId}/likes")
    @Override
    public ApiResponse<?> add(@PathVariable("productId") Long productId) {
        return null;
    }

    @DeleteMapping("/products/{productId}/likes")
    @Override
    public ApiResponse<?> remove(@PathVariable("productId") Long productId) {
        return null;
    }

    @GetMapping("/users/{userId}/likes")
    @Override
    public ApiResponse<?> getMyLikes(@PathVariable("userId") Long userId) {
        return null;
    }
}
