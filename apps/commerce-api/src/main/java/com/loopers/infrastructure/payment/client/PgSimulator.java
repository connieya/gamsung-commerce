package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.*;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.ErrorType;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PgSimulator implements PaymentAdapter {

    private final static String CALLBACK_URL = "http://localhost:8080/api/v1/payments/callback";
    private final PgSimulatorClient client;
    private final PaymentRepository paymentRepository;


    @Override
    @Retry(name = "pgRetry", fallbackMethod = "requestFallback")
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "requestFallback")
    public void request(PaymentCommand.Transaction paymentCommand) {
        PgSimulatorRequest.RequestTransaction requestTransaction = PgSimulatorRequest.RequestTransaction.of(
                paymentCommand.orderId(), paymentCommand.cardNumber(), paymentCommand.amount(), CALLBACK_URL, paymentCommand.cardType());

        try {
            ApiResponse<PgSimulatorResponse.RequestTransaction> response = client.request("12345", requestTransaction);
            PgSimulatorResponse.RequestTransaction data = response.data();
            System.out.println("data.transactionKey() = " + data.transactionKey());
        } catch (FeignException e) {
            System.out.println("FeignException occurred: " + e);
        }

    }

    @Transactional
    public void requestFallback(PaymentCommand.Transaction paymentCommand, Throwable throwable) {
        System.out.println("Fallback method called due to: " + throwable.getMessage());
        Payment payment = paymentRepository.findById(paymentCommand.paymentId())
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(ErrorType.PAYMENT_NOT_FOUND));

        payment.fail();
        throw new PaymentException.PaymentRequestFailedException(ErrorType.PAYMENT_PG_REQUEST_FAILED);
    }
}
