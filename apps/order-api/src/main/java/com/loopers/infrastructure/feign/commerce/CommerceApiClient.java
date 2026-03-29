// [LLD-FEIGN-01] CommerceApiClient — docs/lld/stock-reservation.md > order-api 연동 6-1
package com.loopers.infrastructure.feign.commerce;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "commerce-api", url = "${service.commerce-api.url}")
public interface CommerceApiClient {

    @GetMapping("/internal/v1/users/{userId}")
    ApiResponse<CommerceApiDto.UserResponse> getUser(@PathVariable("userId") String userId);

    @PostMapping("/internal/v1/products/bulk")
    ApiResponse<List<CommerceApiDto.ProductResponse>> getProducts(@RequestBody CommerceApiDto.ProductBulkRequest request);

    @PostMapping("/internal/v1/payments/ready")
    ApiResponse<CommerceApiDto.PaymentReadyResponse> paymentReady(@RequestBody CommerceApiDto.PaymentReadyRequest request);

    // [LLD-FEIGN-01] reserveStock — docs/lld/stock-reservation.md > order-api 연동 6-1
    @PostMapping("/internal/v1/stocks/reserve")
    ApiResponse<CommerceApiDto.StockReserveResponse> reserveStock(
            @RequestBody CommerceApiDto.StockReserveRequest request);

    // [LLD-FEIGN-01] cancelStock — docs/lld/stock-reservation.md > order-api 연동 6-1
    @PostMapping("/internal/v1/stocks/cancel")
    ApiResponse<Void> cancelStock(
            @RequestBody CommerceApiDto.StockCancelRequest request);
}
