// [LLD-API-02] StockInternalV1Controller — docs/lld/stock-reservation.md > API 레이어 5-3
package com.loopers.interfaces.api.stock;

import com.loopers.application.stock.StockFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/stocks")
@RequiredArgsConstructor
public class StockInternalV1Controller implements StockInternalV1ApiSpec {

    private final StockFacade stockFacade;

    // [LLD-API-02] POST /internal/v1/stocks/reserve — docs/lld/stock-reservation.md > API 레이어 5-3
    @PostMapping("/reserve")
    public ApiResponse<StockInternalV1Dto.ReserveResponse> reserve(
            @RequestBody StockInternalV1Dto.ReserveRequest request) {
        stockFacade.reserve(request.toCommand());
        return ApiResponse.success(StockInternalV1Dto.ReserveResponse.of(request.orderId()));
    }

    // [LLD-API-02] POST /internal/v1/stocks/cancel — docs/lld/stock-reservation.md > API 레이어 5-3
    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(
            @RequestBody StockInternalV1Dto.CancelRequest request) {
        stockFacade.cancel(request.toCommand());
        return ApiResponse.success(null);
    }
}
