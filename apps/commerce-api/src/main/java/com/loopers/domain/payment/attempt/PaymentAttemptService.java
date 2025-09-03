package com.loopers.domain.payment.attempt;

import com.loopers.domain.payment.PgStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAttemptService {

    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public void markRequested(AttemptCommand.Request request) {
        PaymentAttempt paymentAttempt = PaymentAttempt.create(request.paymentId(), request.orderNumber(), AttemptStatus.REQUESTED);
        paymentAttemptRepository.save(paymentAttempt);
    }

    @Transactional
    public void markCompleted(AttemptCommand.Complete complete) {
        AttemptStatus attemptStatus = PgStatusMapper.toAttemptStatus(complete.transactionStatus());
        PaymentAttempt paymentAttempt = PaymentAttempt.create(complete.transactionKey(), complete.paymentId(), complete.orderNumber(), attemptStatus);
        paymentAttemptRepository.save(paymentAttempt);

    }

    @Transactional
    public void markFailure(AttemptCommand.Failure failure) {
        PaymentAttempt paymentAttempt = PaymentAttempt.create(failure.paymentId(), failure.orderNumber(), failure.attemptStatus());
        paymentAttemptRepository.save(paymentAttempt);
    }

    @Transactional
    public void markSuccess(AttemptCommand.Success success) {
        PaymentAttempt paymentAttempt = PaymentAttempt.create(success.paymentId(), success.orderNumber(), success.attemptStatus());
        paymentAttemptRepository.save(paymentAttempt);

    }
}
