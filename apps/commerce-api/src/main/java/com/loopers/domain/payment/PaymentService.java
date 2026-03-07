package com.loopers.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.payment.attempt.AttemptStatus;
import com.loopers.domain.payment.attempt.PaymentAttempt;
import com.loopers.domain.payment.attempt.PaymentAttemptRepository;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.domain.payment.exception.PaymentFailure;
import com.loopers.domain.payment.idempotency.IdempotencyKey;
import com.loopers.domain.payment.idempotency.IdempotencyKeyRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void complete(PaymentCommand.Search paymentCommand) {
        PaymentTransactionDetail transactionDetail = paymentClient.getTransactionDetail(paymentCommand);

        Payment payment = paymentRepository.findByOrderNumber(paymentCommand.orderNumber())
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));

        if (transactionDetail.transactionStatus() == TransactionStatus.SUCCESS) {
            payment.paid();
        } else if (transactionDetail.transactionStatus() == TransactionStatus.FAILED) {
            payment.fail();
        } else {
            payment.pending();
        }
    }

    @Transactional
    public void fail(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));
        payment.fail();
    }

    @Transactional
    public void paid(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));
        payment.paid();
    }

    @Transactional(readOnly = true)
    public List<Payment> getPendingPayment(LocalDateTime threshold) {
        return paymentRepository.findByPendingAndCreatedAt(threshold);
    }


    @Transactional
    public PaymentInfo.SessionResult createPaymentSession(PaymentCommand.Transaction transaction, String orderKey) {
        // 멱등성 체크: 이미 처리된 경우 기존 결과 반환
        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByOrderNoAndOrderKeyAndOperationType(
                transaction.orderNumber(), 
                orderKey, 
                IdempotencyKey.OperationType.PAYMENT_SESSION
        );
        
        if (existingKey.isPresent()) {
            try {
                return objectMapper.readValue(
                        existingKey.get().getResultData(),
                        PaymentInfo.SessionResult.class
                );
            } catch (JsonProcessingException e) {
                log.error("[멱등성] PaymentSession 캐시 결과 역직렬화 실패. orderNo={}", transaction.orderNumber(), e);
            }
        }

        // 실제 payment session 생성
        try {
            PaymentRequestResult requestResult = paymentClient.request(transaction);

            // paymentUrl 생성 (pg-simulator가 제공하지 않으면 우리가 생성)
            String paymentUrl = String.format("http://localhost:8082/payment/%s", requestResult.transactionKey());

            PaymentInfo.SessionResult result = new PaymentInfo.SessionResult(
                    transaction.orderNumber(),
                    requestResult.transactionKey(),
                    transaction.amount(),
                    paymentUrl,
                    "VIVAREPUBLICA" // pgKind
            );

            saveIdempotencyKey(transaction.orderNumber(), orderKey, IdempotencyKey.OperationType.PAYMENT_SESSION, result);

            applicationEventPublisher.publishEvent(PaymentEvent.Complete.of(requestResult.transactionKey(), transaction.orderNumber(), requestResult.status(), transaction.couponId(), transaction.userId()));

            return result;
        } catch (CoreException e) {
            AttemptStatus attemptStatus = (e instanceof PaymentFailure pf) ? pf.attemptStatus() : AttemptStatus.FAILED;
            applicationEventPublisher.publishEvent(PaymentEvent.Failure.of(transaction.orderNumber(), attemptStatus));
            throw e;
        }
    }
    
    @Transactional
    public void requestPayment(PaymentCommand.Transaction transaction) {
        try {
            PaymentRequestResult requestResult = paymentClient.request(transaction);
            applicationEventPublisher.publishEvent(PaymentEvent.Complete.of(requestResult.transactionKey(), transaction.orderNumber(), requestResult.status(), transaction.couponId(), transaction.userId()));
        } catch (CoreException e) {
            AttemptStatus attemptStatus = (e instanceof PaymentFailure pf) ? pf.attemptStatus() : AttemptStatus.FAILED;
            applicationEventPublisher.publishEvent(PaymentEvent.Failure.of(transaction.orderNumber(), attemptStatus));

        }
    }
    
    @Transactional
    public Payment ensurePendingPayment(PaymentCommand.Ready ready) {
        Optional<Payment> existingPayment = paymentRepository.findByOrderNumber(ready.orderNumber());
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Payment payment = Payment.create(
                ready.totalAmount(),
                ready.orderId(),
                ready.orderNumber(),
                ready.userId(),
                ready.paymentMethod(),
                PaymentStatus.PENDING
        );
        Payment savedPayment = paymentRepository.save(payment);
        PaymentAttempt paymentAttempt = PaymentAttempt.create(savedPayment.getId(), ready.orderNumber(), AttemptStatus.REQUESTED);
        paymentAttemptRepository.save(paymentAttempt);
        return savedPayment;
    }

    @Transactional
    public PaymentInfo.ReadyResult ready(PaymentCommand.Ready ready, String orderKey) {
        // 멱등성 체크: 이미 처리된 경우 기존 결과 반환
        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByOrderNoAndOrderKeyAndOperationType(
                ready.orderNumber(), 
                orderKey, 
                IdempotencyKey.OperationType.READY
        );
        
        if (existingKey.isPresent()) {
            try {
                return objectMapper.readValue(
                        existingKey.get().getResultData(),
                        PaymentInfo.ReadyResult.class
                );
            } catch (JsonProcessingException e) {
                log.error("[멱등성] PaymentReady 캐시 결과 역직렬화 실패. orderNo={}", ready.orderNumber(), e);
            }
        }

        Payment savedPayment = ensurePendingPayment(ready);
        PaymentInfo.ReadyResult result = new PaymentInfo.ReadyResult(savedPayment.getId(), savedPayment.getPaymentStatus());
        if (orderKey != null) {
            saveIdempotencyKey(ready.orderNumber(), orderKey, IdempotencyKey.OperationType.READY, result);
        }

        return result;
    }

    private <T> void saveIdempotencyKey(String orderNo, String orderKey, IdempotencyKey.OperationType operationType, T result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            IdempotencyKey idempotencyKey = IdempotencyKey.create(orderNo, orderKey, operationType, resultJson);
            idempotencyKeyRepository.save(idempotencyKey);
        } catch (JsonProcessingException e) {
            log.error("[멱등성] 결과 직렬화 실패. orderNo={}, operationType={}", orderNo, operationType, e);
        } catch (DataIntegrityViolationException e) {
            log.warn("[멱등성] 동시 요청으로 중복 키 저장 시도. orderNo={}, operationType={}", orderNo, operationType);
        }
    }
}
