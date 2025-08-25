package com.loopers.infrastructure.payment.attempt;

import com.loopers.domain.payment.attempt.PaymentAttempt;
import com.loopers.domain.payment.attempt.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PaymentAttemptCoreRepository implements PaymentAttemptRepository {

    private final PaymentAttemptJpaRepository paymentAttemptRepository;

    @Override
    public PaymentAttempt save(PaymentAttempt paymentAttempt) {
        return paymentAttemptRepository.save(paymentAttempt);
    }
}
