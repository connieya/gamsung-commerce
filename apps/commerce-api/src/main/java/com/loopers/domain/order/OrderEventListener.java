package com.loopers.domain.order;

import com.loopers.domain.order.exception.OrderException;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderRepository orderRepository;

    @EventListener
    public void onPaymentSuccess(PaymentEvent.Success event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));
        order.paid();
    }


}
