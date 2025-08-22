package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentCoreRepository implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderNumber(String orderNumber) {
        return paymentJpaRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Payment> findByPendingAndCreatedAt(LocalDateTime threshold) {
        return paymentJpaRepository.findByPaymentStatus(threshold);
    }
}
