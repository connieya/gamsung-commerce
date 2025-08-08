package com.loopers.domain.order;

import com.loopers.domain.order.exception.OrderException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderInfo place(OrderCommand orderCommand) {
        Order order = Order.create(orderCommand);

        try {
            Order save = orderRepository.save(order);
            return OrderInfo.from(save);
        }catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.", e);
        }

    }

    @Transactional(readOnly = true)
    public OrderInfo getOrderDetail(Long orderId) {
        Order order = orderRepository.findOrderDetailById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        return OrderInfo.from(order);
    }
}
