package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.payment.PaymentService;
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
    private final PaymentFacade paymentFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Dto.Response.Place> place(@RequestHeader(ApiHeaders.USER_ID) String userId, @RequestBody OrderV1Dto.Request.Place request) {
        OrderCriteria orderCriteria = OrderCriteria.builder()
                .orderNo(request.orderNo())
                .orderSignature(request.orderSignature())
                .orderKey(request.orderKey())
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
        // userId는 Musinsa처럼 인증 컨텍스트를 맞추기 위해 받지만, 현재 발급 로직에는 사용하지 않음.
        OrderResult.IssueOrderNo result = orderFacade.issueOrderNo(request.isNewOrderForm());
        return ApiResponse.success(OrderV1Dto.Response.IssueOrderNo.from(result));
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
    
    @PostMapping("/{orderNo}/ready")
    public ApiResponse<OrderV1Dto.Response.Ready> ready(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.Ready request
    ) {
        List<PaymentCriteria.OrderItem> orderItems = request.orderItems().stream()
                .map(item -> new PaymentCriteria.OrderItem(item.getProductId(), item.getQuantity()))
                .toList();
        PaymentCriteria.Ready criteria = new PaymentCriteria.Ready(
                request.paymentMethod(),
                request.payKind(),
                userId,
                orderItems,
                request.couponId()
        );
        PaymentService.PaymentReadyResult result = paymentFacade.ready(orderNo, request.orderKey(), criteria);
        return ApiResponse.success(OrderV1Dto.Response.Ready.from(result));
    }
    
    @PostMapping("/payment-session")
    public ApiResponse<OrderV1Dto.Response.PaymentSession> paymentSession(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.PaymentSession request
    ) {
        List<PaymentCriteria.OrderItem> orderItems = request.orderItems().stream()
                .map(item -> new PaymentCriteria.OrderItem(item.getProductId(), item.getQuantity()))
                .toList();
        PaymentCriteria.PaymentSession criteria = new PaymentCriteria.PaymentSession(
                request.paymentMethod(),
                request.payKind(),
                userId,
                orderItems,
                request.cardType(),
                request.cardNumber(),
                request.couponId()
        );
        PaymentService.PaymentSessionResult result = paymentFacade.createPaymentSession(
                request.orderNo(),
                request.orderKey(),
                criteria
        );
        return ApiResponse.success(OrderV1Dto.Response.PaymentSession.from(result));
    }
}
