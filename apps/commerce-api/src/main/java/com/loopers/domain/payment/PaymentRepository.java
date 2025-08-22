package com.loopers.domain.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderNumber(String orderNumber);

    List<Payment> findByPendingAndCreatedAt(LocalDateTime threshold);
}
