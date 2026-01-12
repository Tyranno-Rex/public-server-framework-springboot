package com.common.server.common.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate Limiting Interceptor
 *
 * IP 기반 요청 제한을 구현합니다.
 * 제한 값은 application.properties에서 설정 가능합니다.
 *
 * @author DDIP Team
 * @since 2026-01-03
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // 인증 엔드포인트용 버킷 맵
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    // 일반 엔드포인트용 버킷 맵
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    // 인증 엔드포인트: 분당 요청 제한 (application.properties에서 설정)
    @Value("${ratelimit.auth.requests-per-minute:10}")
    private int authRequestsPerMinute;

    // 일반 엔드포인트: 분당 요청 제한 (application.properties에서 설정)
    @Value("${ratelimit.general.requests-per-minute:60}")
    private int generalRequestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIP(request);
        String requestUri = request.getRequestURI();

        Bucket bucket = resolveBucket(ip, requestUri);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for client: {}, URI: {}", ip, requestUri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return false;
        }
    }

    /**
     * 클라이언트 IP 추출 (프록시 환경 고려)
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For의 경우 여러 IP가 쉼표로 구분될 수 있음 (첫 번째가 실제 클라이언트)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Bucket 생성 또는 조회
     */
    private Bucket resolveBucket(String ip, String requestUri) {
        if (isAuthEndpoint(requestUri)) {
            return authBuckets.computeIfAbsent(ip, k -> createAuthBucket());
        } else {
            return generalBuckets.computeIfAbsent(ip, k -> createGeneralBucket());
        }
    }

    /**
     * 인증 엔드포인트용 Rate Limit Bucket 생성
     */
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(authRequestsPerMinute)
                .refillIntervally(authRequestsPerMinute, Duration.ofMinutes(1))
                .build();
        log.debug("Created new auth rate limit bucket.");
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * 일반 엔드포인트용 Rate Limit Bucket 생성
     */
    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(generalRequestsPerMinute)
                .refillIntervally(generalRequestsPerMinute, Duration.ofMinutes(1))
                .build();
        log.debug("Created new general rate limit bucket.");
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * 인증 관련 엔드포인트 여부 확인
     */
    private boolean isAuthEndpoint(String requestUri) {
        return requestUri.startsWith("/api/auth/login") ||
               requestUri.startsWith("/api/auth/refresh");
    }
}
