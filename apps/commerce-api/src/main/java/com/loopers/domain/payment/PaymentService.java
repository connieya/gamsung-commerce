package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentAdapter paymentAdapter;

    @Transactional
    public Payment create(PaymentCommand.Create paymentCommand, PaymentStatus paymentStatus) {
        User user = userRepository.findByUserId(paymentCommand.userId())
                .orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        Order order = orderRepository.findById(paymentCommand.orderId())
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        Payment payment = Payment.create(
                paymentCommand.finalAmount()
                , paymentCommand.orderId()
                , order.getOrderNumber()
                , user.getId()
                , paymentCommand.paymentMethod()
                , paymentStatus);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void complete(PaymentCommand.Search paymentCommand) {
        PgSimulatorResponse.TransactionDetail transactionDetail = paymentAdapter.getTransactionDetail(paymentCommand);

        Payment payment = paymentRepository.findByOrderNumber(paymentCommand.orderNumber())
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));

        if (transactionDetail.transactionStatus() == TransactionStatus.SUCCESS) {
            payment.paid();
        } else if (transactionDetail.transactionStatus() == TransactionStatus.FAILED) {
            payment.fail();
        } else {
            payment.pending();
        }
    }

    @Transactional
    public void fail(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));
        payment.fail();
    }

    @Transactional
    public void paid(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));
        payment.paid();
    }
}
