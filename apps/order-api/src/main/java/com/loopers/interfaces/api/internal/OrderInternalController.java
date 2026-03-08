package com.loopers.interfaces.api.internal;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ApiResponse<OrderInternalDto.OrderResponse> getOrder(@PathVariable("orderId") Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ApiResponse.success(OrderInternalDto.OrderResponse.from(order));
    }

    @GetMapping("/by-order-number/{orderNo}")
    public ApiResponse<OrderInternalDto.OrderResponse> getOrderByOrderNumber(@PathVariable("orderNo") String orderNo) {
        Order order = orderService.getOrderByOrderNumber(orderNo);
        return ApiResponse.success(OrderInternalDto.OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable("orderId") Long orderId) {
        orderService.complete(orderId);
        return ApiResponse.success(null);
    }
}
