// [LLD-EVT-01] StockEventListener — docs/lld/stock-reservation.md > 도메인 레이어 2-7
package com.loopers.domain.stock;

import com.loopers.domain.payment.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockEventListener {

    private final StockService stockService;

    // [LLD-EVT-01] deduct() -> confirm() 교체 — docs/lld/stock-reservation.md > 도메인 레이어 2-7
    @EventListener
    public void onPaymentSuccess(PaymentEvent.Success event) {
        stockService.confirm(StockCommand.ConfirmReservation.of(event.orderId()));
    }
}
