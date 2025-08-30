package com.loopers.infrastructure.payment.attempt;

import com.loopers.domain.payment.attempt.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttempt, Long> {
}
