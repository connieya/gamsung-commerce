package com.loopers.infrastructure.payment.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.exception.PaymentException;
import feign.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PgSimulatorTest {

    private static final WireMockServer mockServer = new WireMockServer(8082);

    private static final int REQUIRED_FAILURES_TO_OPEN_CIRCUIT = 2;

    @Autowired
    private PgSimulator pgSimulator;


    @BeforeEach
    void setUp() {
        mockServer.start();
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    @DisplayName("정해진 횟수 이상 타임아웃 발생 시 서킷이 열리고 CircuitOpenException을 던진다.")
    void circuitOpens_andThrowsException_afterMultipleTimeouts() {
        mockServer.stubFor(post("/api/v1/payments")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"meta\": {\"result\": \"SUCCESS\"}}")
                        .withFixedDelay(4000)
                ));

        PaymentCommand.Transaction transaction = PaymentCommand.Transaction.of(1L, "12345", CardType.HYUNDAI, "1234-5678-9012-3456", 1000L, 1L, 1L);


        for (int i = 0; i < REQUIRED_FAILURES_TO_OPEN_CIRCUIT; i++) {
            assertThatThrownBy(() -> pgSimulator.request(transaction))
                    .isInstanceOf(PaymentException.PgTimeoutException.class);

        }

        assertThatThrownBy(() -> pgSimulator.request(transaction))
                .isInstanceOf(PaymentException.CircuitOpenException.class);

    }


    @TestConfiguration
    static class FeignTestConfig {
        @Bean("testFeignOptions")
        public Request.Options feignOptions() {
            return new Request.Options(
                    1000,
                    2000
            );
        }
    }
}

