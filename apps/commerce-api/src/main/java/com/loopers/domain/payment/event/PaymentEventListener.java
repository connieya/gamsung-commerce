package com.loopers.domain.payment.event;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.attempt.AttemptCommand;
import com.loopers.domain.payment.attempt.PaymentAttemptService;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentAttemptService paymentAttemptService;
    private final PaymentRepository paymentRepository;

    @EventListener
    public void recordTransactionRequest(PaymentEvent.Ready event) {
        paymentAttemptService.markRequested(AttemptCommand.Request.of(event.paymentId(), event.orderNumber()));
    }

    @EventListener
    public void recordTransactionComplete(PaymentEvent.Complete event) {
        paymentAttemptService.markCompleted(AttemptCommand.Complete.of(event.transactionKey(),event.paymentId(),event.orderNumber(),event.status()));
        Payment payment = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));
        payment.execute(event.status());
    }

    @EventListener
    public void recordTransactionFailure(PaymentEvent.Failure event) {
        paymentAttemptService.markFailure(AttemptCommand.Failure.of(event.paymentId(),event.orderNumber(),event.status()));
        Payment payment = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));
        payment.fail();
    }

}
