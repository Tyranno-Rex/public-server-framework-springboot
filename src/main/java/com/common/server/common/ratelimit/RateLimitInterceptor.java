package com.common.server.common.ratelimit;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.common.server.dto.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
 * <p><strong>보안 주의사항:</strong>
 * X-Forwarded-For 헤더 스푸핑 방지를 위해 신뢰할 수 있는 프록시 IP를 설정하세요.</p>
 *
 * @author DDIP Team
 * @since 2026-01-03
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // IP 주소 유효성 검증 패턴 (IPv4 & IPv6)
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::$|^::1$|^([0-9a-fA-F]{1,4}:){1,7}:$");

    // 인증 엔드포인트용 버킷 맵
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    // 일반 엔드포인트용 버킷 맵
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    // 신뢰할 수 있는 프록시 IP 목록 (기본: 로컬호스트)
    private final Set<String> trustedProxies = Set.of(
            "127.0.0.1", "::1", "localhost"
    );

    // 인증 엔드포인트: 분당 요청 제한 (application.properties에서 설정)
    @Value("${ratelimit.auth.requests-per-minute:10}")
    private int authRequestsPerMinute;

    // 일반 엔드포인트: 분당 요청 제한 (application.properties에서 설정)
    @Value("${ratelimit.general.requests-per-minute:60}")
    private int generalRequestsPerMinute;

    // 신뢰할 수 있는 프록시 IP 추가 (application.properties에서 설정)
    @Value("${ratelimit.trusted-proxies:}")
    private List<String> additionalTrustedProxies;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIP(request);
        String requestUri = request.getRequestURI();

        Bucket bucket = resolveBucket(ip, requestUri);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for client: {}, URI: {}", ip, requestUri);
            writeRateLimitErrorResponse(response, request);
            return false;
        }
    }

    /**
     * Rate Limit 초과 시 표준 ErrorResponse 형식으로 응답
     */
    private void writeRateLimitErrorResponse(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .code("RATE_LIMIT_EXCEEDED")
                .message("요청 횟수가 제한을 초과했습니다. 잠시 후 다시 시도해주세요.")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 클라이언트 IP 추출 (프록시 환경 고려, 스푸핑 방지)
     *
     * <p>X-Forwarded-For 헤더는 신뢰할 수 있는 프록시에서 온 요청에만 사용합니다.</p>
     */
    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // 신뢰할 수 있는 프록시에서 온 요청만 X-Forwarded-For 헤더 사용
        if (isTrustedProxy(remoteAddr)) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
                // 첫 번째 IP가 실제 클라이언트
                String clientIp = forwardedFor.split(",")[0].trim();
                if (isValidIpAddress(clientIp)) {
                    return clientIp;
                }
                log.warn("Invalid IP in X-Forwarded-For header: {}", clientIp);
            }
        }

        return remoteAddr;
    }

    /**
     * 신뢰할 수 있는 프록시인지 확인
     */
    private boolean isTrustedProxy(String ip) {
        if (trustedProxies.contains(ip)) {
            return true;
        }
        if (additionalTrustedProxies != null) {
            return additionalTrustedProxies.contains(ip);
        }
        return false;
    }

    /**
     * 유효한 IP 주소인지 검증 (IPv4 또는 IPv6)
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
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
