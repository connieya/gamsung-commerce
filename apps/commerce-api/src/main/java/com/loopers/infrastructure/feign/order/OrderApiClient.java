package com.loopers.infrastructure.feign.order;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-api", url = "${service.order-api.url}")
public interface OrderApiClient {

    @GetMapping("/internal/v1/orders/{orderId}")
    ApiResponse<OrderApiDto.OrderResponse> getOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/internal/v1/orders/by-order-number/{orderNo}")
    ApiResponse<OrderApiDto.OrderResponse> getOrderByOrderNumber(@PathVariable("orderNo") String orderNo);

    @PostMapping("/internal/v1/orders/{orderId}/complete")
    ApiResponse<Void> completeOrder(@PathVariable("orderId") Long orderId);
}
