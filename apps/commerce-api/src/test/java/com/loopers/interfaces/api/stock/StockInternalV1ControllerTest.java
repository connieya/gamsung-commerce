// [LLD-TEST-04] StockInternalV1ControllerTest — docs/lld/stock-reservation.md > 테스트 전략 > API 테스트
package com.loopers.interfaces.api.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.stock.StockFacade;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.interfaces.api.ApiControllerAdvice;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockInternalV1Controller.class)
@Import(ApiControllerAdvice.class)
class StockInternalV1ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    StockFacade stockFacade;

    // [LLD-TEST-04] POST /internal/v1/stocks/reserve → 200 OK (AC-01) — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("재고 선점 성공 시 200 OK와 PENDING 상태를 반환한다.")
    void reserve_success() throws Exception {
        // given
        StockInternalV1Dto.ReserveRequest request = new StockInternalV1Dto.ReserveRequest(
                1L,
                List.of(new StockInternalV1Dto.ReserveRequest.Item(10L, 5L))
        );
        doNothing().when(stockFacade).reserve(any());

        // when & then
        mockMvc.perform(post("/internal/v1/stocks/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    // [LLD-TEST-04] POST /internal/v1/stocks/reserve (재고 부족) → 400 STOCK_INSUFFICIENT (AC-02) — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("재고 부족 시 400 STOCK_INSUFFICIENT를 반환한다.")
    void reserve_fail_insufficientStock() throws Exception {
        // given
        StockInternalV1Dto.ReserveRequest request = new StockInternalV1Dto.ReserveRequest(
                1L,
                List.of(new StockInternalV1Dto.ReserveRequest.Item(10L, 100L))
        );
        doThrow(new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT))
                .when(stockFacade).reserve(any());

        // when & then
        mockMvc.perform(post("/internal/v1/stocks/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.meta.result").value("FAIL"))
                .andExpect(jsonPath("$.meta.errorCode").value("Stock Insufficient"));
    }

    // [LLD-TEST-04] POST /internal/v1/stocks/cancel → 200 OK (AC-05) — docs/lld/stock-reservation.md > 테스트 전략
    @Test
    @DisplayName("재고 선점 취소 성공 시 200 OK를 반환한다.")
    void cancel_success() throws Exception {
        // given
        StockInternalV1Dto.CancelRequest request = new StockInternalV1Dto.CancelRequest(1L);
        doNothing().when(stockFacade).cancel(any());

        // when & then
        mockMvc.perform(post("/internal/v1/stocks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"));
    }
}
