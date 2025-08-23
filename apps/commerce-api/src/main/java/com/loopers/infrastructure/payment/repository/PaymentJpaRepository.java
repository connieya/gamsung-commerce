package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByOrderNumber(String orderNumber);

    @Query("select p from Payment p where p.paymentStatus = 'PENDING' and p.createdAt < :threshold")
    List<Payment> findByPaymentStatus(LocalDateTime threshold);
}
