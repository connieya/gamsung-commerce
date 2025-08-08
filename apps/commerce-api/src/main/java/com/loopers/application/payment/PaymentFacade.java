package com.loopers.application.payment;

import com.loopers.domain.coupon.UserCouponCommand;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.coupon.UserCouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.stock.StockService;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final UserCouponService userCouponService;


    @Transactional
    public PaymentResult pay(PaymentCriteria.Pay criteria) {
        Order order = orderRepository.findById(criteria.orderId())
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        // 포인트 차감
        pointService.deduct(criteria.userId(), order.getFinalAmount());
        List<OrderLine> orderLines = order.getOrderLines();

        // 재고 차감
        stockService.deduct(orderLines);

        if (order.getDiscountAmount() > 0L) {
            userCouponService.use(UserCouponCommand.of(criteria.userId()));
        }

        Payment pay = paymentService.pay(criteria.toCommand());

        return PaymentResult.from(pay);
    }

}
