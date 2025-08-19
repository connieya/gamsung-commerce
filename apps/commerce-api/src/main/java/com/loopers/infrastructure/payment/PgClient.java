package com.loopers.infrastructure.payment;

import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "pgClient", url = "http://localhost:8082" , configuration = PgClientConfig.class)
public interface PgClient {

    @PostMapping("/api/v1/payments")
    ApiResponse<?> request(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );
}
