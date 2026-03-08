package com.loopers.support.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 런타임 로그 모드 전환 API (local, local-dev, dev 전용)
 *
 * BUSINESS 모드: 비즈니스 로직(com.loopers) 로그만 표시, 프레임워크 노이즈 억제
 * VERBOSE  모드: Hibernate SQL, 바인딩 값, 트랜잭션 경계 등 전체 로그 표시
 */
@RestController
@Profile({"local", "local-dev", "dev"})
@RequestMapping("/api/internal/log-mode")
public class LogModeController {

    public enum LogMode { BUSINESS, VERBOSE }

    private static final Map<String, Level> VERBOSE_LEVELS = Map.of(
            "org.springframework.transaction.interceptor", Level.TRACE,
            "org.springframework.orm.jpa.JpaTransactionManager", Level.DEBUG,
            "org.springframework.jdbc.datasource.DataSourceTransactionManager", Level.DEBUG,
            "org.hibernate.SQL", Level.DEBUG,
            "org.hibernate.orm.jdbc.bind", Level.TRACE
    );

    private static final Level BUSINESS_LEVEL = Level.WARN;

    private LogMode currentMode = LogMode.BUSINESS;

    @GetMapping
    public Map<String, Object> getMode() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", currentMode);
        result.put("description", descriptionOf(currentMode));
        return result;
    }

    @PutMapping("/{mode}")
    public Map<String, Object> setMode(@PathVariable LogMode mode) {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (mode == LogMode.VERBOSE) {
            VERBOSE_LEVELS.forEach((name, level) -> ctx.getLogger(name).setLevel(level));
        } else {
            VERBOSE_LEVELS.keySet().forEach(name -> ctx.getLogger(name).setLevel(BUSINESS_LEVEL));
        }

        currentMode = mode;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", currentMode);
        result.put("description", descriptionOf(currentMode));

        Map<String, String> loggers = new LinkedHashMap<>();
        VERBOSE_LEVELS.keySet().forEach(name -> {
            Level effective = ctx.getLogger(name).getEffectiveLevel();
            loggers.put(name, effective.toString());
        });
        result.put("loggers", loggers);

        return result;
    }

    private String descriptionOf(LogMode mode) {
        return switch (mode) {
            case BUSINESS -> "비즈니스 로직만 표시 (Hibernate SQL, 트랜잭션 로그 숨김)";
            case VERBOSE -> "전체 로그 표시 (SQL, 바인딩 값, 트랜잭션 경계 포함)";
        };
    }
}
