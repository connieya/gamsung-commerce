package com.loopers.support.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.StringJoiner;

@Aspect
@Component
@Profile({"local", "local-dev", "dev"})
public class LayerLoggingAspect {

    private static final int MAX_STRING_LENGTH = 50;

    // ANSI 색상
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String RED = "\u001B[31m";
    private static final String GRAY = "\u001B[90m";

    @Pointcut("execution(* com.loopers.application..*(..))")
    private void applicationLayer() {}

    @Pointcut("execution(* com.loopers.domain..*(..))")
    private void domainLayer() {}

    @Pointcut("execution(* *..*.repository..*(..))")
    private void repositoryMethods() {}

    @Around("applicationLayer()")
    public Object logApplicationLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "APPLICATION", BLUE);
    }

    @Around("domainLayer() && !repositoryMethods()")
    public Object logDomainLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "DOMAIN", GREEN);
    }

    private Object logExecution(ProceedingJoinPoint joinPoint, String layer, String color) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = summarizeArgs(joinPoint.getArgs());

        log.debug("{}{}[{}]{} {}{}.{}{} {} (args: {})",
                BOLD, color, layer, RESET,
                CYAN, className, methodName, RESET,
                YELLOW + "▶" + RESET, args);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            log.debug("{}{}[{}]{} {}{}.{}{} {} ({}{}ms{}, return: {})",
                    BOLD, color, layer, RESET,
                    CYAN, className, methodName, RESET,
                    GREEN + "✓" + RESET,
                    GRAY, elapsed, RESET,
                    summarizeResult(result));

            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.debug("{}{}[{}]{} {}{}.{}{} {} ({}{}ms{}, exception: {}{}{})",
                    BOLD, color, layer, RESET,
                    CYAN, className, methodName, RESET,
                    RED + "✗" + RESET,
                    GRAY, elapsed, RESET,
                    RED, e.getClass().getSimpleName(), RESET);
            throw e;
        }
    }

    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Object arg : args) {
            joiner.add(summarizeValue(arg));
        }
        return joiner.toString();
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        return summarizeValue(result);
    }

    private String summarizeValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return s.length() > MAX_STRING_LENGTH
                    ? "\"" + s.substring(0, MAX_STRING_LENGTH) + "...\""
                    : "\"" + s + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Collection<?> c) {
            return c.getClass().getSimpleName() + "[" + c.size() + "]";
        }
        return value.getClass().getSimpleName();
    }
}
