package com.loopers.support.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String MDC_METHOD = "method";
    private static final String MDC_REQUEST_URI = "requestUri";
    private static final String MDC_CLIENT_IP = "clientIp";
    private static final String MDC_USER_ID = "userId";

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_USER_ID = "X-USER-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            MDC.put(MDC_METHOD, request.getMethod());
            MDC.put(MDC_REQUEST_URI, request.getRequestURI());
            MDC.put(MDC_CLIENT_IP, extractClientIp(request));

            String userId = request.getHeader(HEADER_X_USER_ID);
            if (userId != null && !userId.isBlank()) {
                MDC.put(MDC_USER_ID, userId);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_METHOD);
            MDC.remove(MDC_REQUEST_URI);
            MDC.remove(MDC_CLIENT_IP);
            MDC.remove(MDC_USER_ID);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
