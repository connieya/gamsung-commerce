package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    @PostMapping
    @Override
    public ApiResponse<?> place() {
        return null;
    }

    @GetMapping
    @Override
    public ApiResponse<?> getOrders(String userId) {
        return null;
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<?> getOrder(@PathVariable("orderId") Long orderId) {
        return null;
    }
}
