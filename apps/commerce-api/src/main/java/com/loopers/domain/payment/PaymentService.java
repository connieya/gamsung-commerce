package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.domain.payment.attempt.AttemptStatus;
import com.loopers.domain.payment.attempt.PaymentAttempt;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.domain.payment.exception.PaymentFailure;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAdapter paymentAdapter;
    private final ApplicationEventPublisher applicationEventPublisher;

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

    @Transactional(readOnly = true)
    public List<Payment> getPendingPayment(LocalDateTime threshold) {
        return paymentRepository.findByPendingAndCreatedAt(threshold);
    }


    @Transactional
    public void requestPayment(PaymentCommand.Transaction transaction) {
        try {
            PgSimulatorResponse.RequestTransaction requestTransaction = paymentAdapter.request(transaction);
            applicationEventPublisher.publishEvent(PaymentEvent.Complete.of(requestTransaction.transactionKey(), transaction.orderNumber(), requestTransaction.status()));
        } catch (CoreException e) {
            AttemptStatus attemptStatus = (e instanceof PaymentFailure pf) ? pf.attemptStatus() : AttemptStatus.FAILED;
            applicationEventPublisher.publishEvent(PaymentEvent.Failure.of(transaction.orderNumber(), attemptStatus));

        }
    }
}
