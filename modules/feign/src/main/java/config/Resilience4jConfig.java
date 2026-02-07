package config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Resilience4jConfig {

    private final RetryRegistry retryRegistry;

    public Resilience4jConfig(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void postConstruct() {
        Retry retry = retryRegistry.retry("pgRetry");

        retry.getEventPublisher()
                .onRetry(event -> log.warn("재시도 발생! 이름: {}, 시도 횟수: {}, 대기 시간: {}, 마지막 예외: {}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getWaitInterval(),
                        event.getLastThrowable()))
                .onSuccess(event -> log.info("최종 성공. 이름: {}, 시도 횟수: {}",
                        event.getName(),
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("최종 실패. 이름: {}, 시도 횟수: {}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable()));
    }
}
