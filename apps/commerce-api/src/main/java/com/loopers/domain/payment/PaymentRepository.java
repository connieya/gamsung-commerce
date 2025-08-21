package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderNumber(String orderNumber);
}
