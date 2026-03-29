// [LLD-API-01] StockInternalV1ApiSpec — docs/lld/stock-reservation.md > API 레이어 5-2
package com.loopers.interfaces.api.stock;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;

public interface StockInternalV1ApiSpec {

    // [LLD-API-01] reserve spec — docs/lld/stock-reservation.md > API 레이어 5-2
    @Operation(summary = "재고 선점", description = "주문 생성 시 재고를 선점한다.")
    ApiResponse<StockInternalV1Dto.ReserveResponse> reserve(
            @RequestBody StockInternalV1Dto.ReserveRequest request);

    // [LLD-API-01] cancel spec — docs/lld/stock-reservation.md > API 레이어 5-2
    @Operation(summary = "재고 선점 취소", description = "주문 취소 시 선점된 재고를 해제한다.")
    ApiResponse<Void> cancel(
            @RequestBody StockInternalV1Dto.CancelRequest request);
}
