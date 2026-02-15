package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Dto.Response.Place> place(@RequestHeader(ApiHeaders.USER_ID) String userId, @RequestBody OrderV1Dto.Request.Place request) {
        OrderCriteria orderCriteria = OrderCriteria.builder()
                .couponId(request.couponId())
                .orderItems(request.orderItems()
                        .stream()
                        .map(o -> OrderCriteria.OrderItem.builder()
                                .productId(o.getProductId())
                                .quantity(o.getQuantity())
                                .build()
                        ).toList())
                .userId(userId)
                .build();

        OrderResult.Create place = orderFacade.place(orderCriteria);
        return ApiResponse.success(OrderV1Dto.Response.Place.from(place));
    }

    @GetMapping
    @Override
    public ApiResponse<OrderV1Dto.Response.List> getOrders(@RequestHeader(ApiHeaders.USER_ID) String userId) {
        OrderResult.List orders = orderFacade.getOrders(userId);
        return ApiResponse.success(OrderV1Dto.Response.List.from(orders));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.Response.Detail> getOrder(@PathVariable("orderId") Long orderId) {
        OrderResult.GetDetail orderDetail = orderFacade.getOrderDetail(orderId);
        return ApiResponse.success(OrderV1Dto.Response.Detail.from(orderDetail));
    }
}
