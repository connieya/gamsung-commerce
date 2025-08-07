package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PointRepository pointRepository;

    public void pay(PaymentCommand paymentCommand) {
        User user = userRepository.findByUserId(paymentCommand.getUserId()).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        Order order = orderRepository.findOrderDetailById(paymentCommand.getOrderId())
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        Payment payment = Payment.create(0L, order.getId(), user.getId(), PaymentMethod.POINT);
        paymentRepository.save(payment);
    }
}
