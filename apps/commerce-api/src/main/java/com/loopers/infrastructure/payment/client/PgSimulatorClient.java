package com.loopers.infrastructure.payment.client;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "pg-simulator-client", url = "http://localhost:8082" , configuration = PgClientConfig.class)
public interface PgSimulatorClient {

    String HEADER_USER_ID = "X-USER-ID";

    @PostMapping("/api/v1/payments")
    ApiResponse<PgSimulatorResponse.RequestTransaction> request(
            @RequestHeader(HEADER_USER_ID) String userId,
            PgSimulatorRequest.RequestTransaction requestTransaction
    );

    @GetMapping("/api/v1/payments/{transactionKey}")
    ApiResponse<PgSimulatorResponse.TransactionDetail> getTransaction(@RequestHeader(HEADER_USER_ID) String userId, @PathVariable String transactionKey);
}
