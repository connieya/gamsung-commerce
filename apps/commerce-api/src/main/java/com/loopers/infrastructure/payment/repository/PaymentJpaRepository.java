package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment,Long> {
}
