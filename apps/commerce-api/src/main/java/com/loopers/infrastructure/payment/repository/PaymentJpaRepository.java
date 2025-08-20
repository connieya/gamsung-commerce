package com.loopers.infrastructure.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity ,Long> {
}
