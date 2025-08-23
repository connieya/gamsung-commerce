package com.loopers.infrastructure.payment.client;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgClientConfig {
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                1000, // 연결 타임아웃 (1초)
                2000  // 읽기 타임아웃 (2초)
        );
    }

}
