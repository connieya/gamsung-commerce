package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Order V1 API", description = "주문 관련 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(
            summary = "주문 요청",
            description = "상품을 주문합니다."
    )
    ApiResponse<OrderV1Dto.Response.Place> place(@RequestHeader(ApiHeaders.USER_ID) String userId, @RequestBody OrderV1Dto.Request.Place request);


    @Operation(
            summary = "유저의 주문 목록 조회",
            description = "유저 ID로 주문 목록을 조회합니다."
    )
    ApiResponse<?> getOrders(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );

    @Operation(
            summary = "단일 주문 상세 조회 ",
            description = "주문 ID로 단일 주문 상세 정보를 조회합니다."
    )
    ApiResponse<?> getOrder(
            @PathVariable("orderId") Long orderId
    );

}
