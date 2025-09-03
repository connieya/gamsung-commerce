package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.*;
import com.loopers.domain.payment.exception.PaymentException;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.ErrorType;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
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


    @Override
    @Retry(name = "pgRetry")
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "requestFallback")
    public PgSimulatorResponse.RequestTransaction request(PaymentCommand.Transaction paymentCommand) {
        PgSimulatorRequest.RequestTransaction requestTransaction = PgSimulatorRequest.RequestTransaction.of(
                paymentCommand.orderNumber(), paymentCommand.cardNumber(), paymentCommand.amount(), CALLBACK_URL, paymentCommand.cardType());

        ApiResponse<PgSimulatorResponse.RequestTransaction> response = client.request("12345", requestTransaction);
        return response.data();

    }


    public PgSimulatorResponse.RequestTransaction requestFallback(PaymentCommand.Transaction paymentCommand, Throwable throwable) {
        if (throwable instanceof feign.RetryableException) {
            throw new PaymentException.PgTimeoutException(ErrorType.PAYMENT_PG_TIMEOUT);
        }

        if (throwable instanceof CallNotPermittedException) {
            throw new PaymentException.CircuitOpenException(ErrorType.PAYMENT_PG_CIRCUIT_OPEN);
        }

        throw new PaymentException.PaymentRequestFailedException(ErrorType.PAYMENT_PG_REQUEST_FAILED);
    }

    @Override
    public PgSimulatorResponse.TransactionDetail getTransactionDetail(PaymentCommand.Search paymentCommand) {
        ApiResponse<PgSimulatorResponse.TransactionDetail> response = client.getTransaction("12345", paymentCommand.transactionKey());
        return response.data();

    }
}
