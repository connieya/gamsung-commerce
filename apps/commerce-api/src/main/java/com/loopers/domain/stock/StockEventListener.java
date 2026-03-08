package com.loopers.domain.stock;

import com.loopers.domain.payment.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class StockEventListener {

    private final StockService stockService;


    @EventListener
    public void onPaymentSuccess(PaymentEvent.Success event) {
        List<PaymentEvent.OrderLineSnapshot> orderLines = event.orderLines();
        List<StockCommand.DeductStocks.Item> items = orderLines.stream()
                .map(orderLine -> StockCommand.DeductStocks.Item
                        .builder()
                        .productId(orderLine.productId())
                        .quantity(orderLine.quantity())
                        .build()
                ).toList();

        stockService.deduct(StockCommand.DeductStocks.create(items));
    }
}
