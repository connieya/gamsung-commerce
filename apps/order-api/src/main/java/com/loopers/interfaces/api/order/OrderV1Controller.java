package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.infrastructure.feign.commerce.CommerceApiDto;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller {

    private final OrderFacade orderFacade;

    @GetMapping("/order-form")
    public ApiResponse<OrderV1Dto.Response.OrderForm> getOrderForm(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestParam(value = "cartItemIds", required = false) List<Long> cartItemIds,
            @RequestParam(value = "t", required = false) Long timestamp
    ) {
        OrderResult.OrderForm result = orderFacade.getOrderForm(userId, cartItemIds);
        return ApiResponse.success(OrderV1Dto.Response.OrderForm.from(result));
    }

    @PostMapping("/order-no")
    public ApiResponse<OrderV1Dto.Response.IssueOrderNo> issueOrderNo(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.IssueOrderNo request
    ) {
        OrderResult.IssueOrderNo result = orderFacade.issueOrderNo(request.isNewOrderForm());
        return ApiResponse.success(OrderV1Dto.Response.IssueOrderNo.from(result));
    }

    @GetMapping
    public ApiResponse<OrderV1Dto.Response.List> getOrders(@RequestHeader(ApiHeaders.USER_ID) String userId) {
        OrderResult.List orders = orderFacade.getOrders(userId);
        return ApiResponse.success(OrderV1Dto.Response.List.from(orders));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderV1Dto.Response.Detail> getOrder(@PathVariable("orderId") Long orderId) {
        OrderResult.GetDetail orderDetail = orderFacade.getOrderDetail(orderId);
        return ApiResponse.success(OrderV1Dto.Response.Detail.from(orderDetail));
    }

    @DeleteMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable("orderId") Long orderId) {
        orderFacade.cancel(orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderNo}/ready")
    public ApiResponse<OrderV1Dto.Response.Ready> ready(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.Ready request
    ) {
        List<OrderCriteria.OrderItem> orderItems = request.orderItems().stream()
                .map(item -> new OrderCriteria.OrderItem(item.getProductId(), item.getQuantity()))
                .toList();
        OrderCriteria.Ready criteria = new OrderCriteria.Ready(
                request.paymentMethod(),
                request.payKind(),
                userId,
                orderItems,
                request.couponId()
        );
        CommerceApiDto.PaymentReadyResponse result = orderFacade.ready(orderNo, request.orderKey(), criteria);
        return ApiResponse.success(new OrderV1Dto.Response.Ready(result.paymentId(), result.paymentStatus()));
    }
}
