package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.*;
import com.loopers.interfaces.api.ApiResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
            System.out.println("data.reason() = " + data.reason());
            System.out.println("data.transactionKey() = " + data.transactionKey());
        } catch (FeignException e) {
            System.out.println("FeignException occurred: " + e);
        }

    }

    public void requestFallback(PaymentCommand.Transaction paymentCommand, Throwable throwable) {
        System.out.println("Fallback method called due to: " + throwable.getMessage());
        // Handle the fallback logic here, e.g., log the error or return a default response
        throw new RuntimeException("Payment request failed, fallback executed", throwable);
    }
}
