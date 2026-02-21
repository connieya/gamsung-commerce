package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Order V1 API", description = "주문 관련 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 요청", description = "상품을 주문합니다.")
    ApiResponse<OrderV1Dto.Response.Place> place(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.Place request
    );

    @Operation(summary = "유저의 주문 목록 조회", description = "유저 ID로 주문 목록을 조회합니다.")
    ApiResponse<OrderV1Dto.Response.List> getOrders(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );

    @Operation(summary = "단일 주문 상세 조회", description = "주문 ID로 단일 주문 상세 정보를 조회합니다.")
    ApiResponse<OrderV1Dto.Response.Detail> getOrder(
            @PathVariable("orderId") Long orderId
    );

    @Operation(summary = "주문서 조회", description = "장바구니 기반 주문서를 조회합니다.")
    ApiResponse<OrderV1Dto.Response.OrderForm> getOrderForm(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestParam(value = "cartItemIds", required = false) List<Long> cartItemIds,
            @RequestParam(value = "t", required = false) Long timestamp
    );

    @Operation(summary = "주문번호 발급", description = "새로운 주문번호와 서명을 발급합니다.")
    ApiResponse<OrderV1Dto.Response.IssueOrderNo> issueOrderNo(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.IssueOrderNo request
    );

    @Operation(summary = "결제 준비", description = "주문 정보를 서버에 등록하고 결제를 준비합니다.")
    ApiResponse<OrderV1Dto.Response.Ready> ready(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.Ready request
    );

    @Operation(summary = "결제 세션 생성", description = "PG사 결제 URL을 확보합니다.")
    ApiResponse<OrderV1Dto.Response.PaymentSession> paymentSession(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @RequestBody OrderV1Dto.Request.PaymentSession request
    );
}
