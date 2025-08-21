package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByOrderNumber(String orderNumber);
}
