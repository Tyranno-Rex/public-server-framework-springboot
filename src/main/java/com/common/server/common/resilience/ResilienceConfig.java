package com.common.server.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Resilience4j 설정
 *
 * Circuit Breaker, Retry 패턴을 위한 설정입니다.
 * 외부 API 호출 시 장애 대응을 위해 사용합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Configuration
@Slf4j
public class ResilienceConfig {

    /**
     * 기본 Circuit Breaker 설정
     */
    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // 실패율 임계치 (50% 이상 실패 시 OPEN)
                .failureRateThreshold(50)
                // 슬라이딩 윈도우 타입
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 슬라이딩 윈도우 크기 (최근 10개 요청 기준)
                .slidingWindowSize(10)
                // 최소 호출 횟수 (이 횟수 이상 호출되어야 상태 변경 검토)
                .minimumNumberOfCalls(5)
                // OPEN 상태 유지 시간
                .waitDurationInOpenState(Duration.ofSeconds(30))
                // HALF_OPEN 상태에서 허용할 요청 수
                .permittedNumberOfCallsInHalfOpenState(3)
                // 느린 호출 임계치 (응답 시간)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                // 느린 호출 비율 임계치
                .slowCallRateThreshold(80)
                // 실패로 기록할 예외
                .recordExceptions(IOException.class, TimeoutException.class, RuntimeException.class)
                // 실패로 기록하지 않을 예외
                .ignoreExceptions(IllegalArgumentException.class)
                // 자동으로 HALF_OPEN으로 전환
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultCircuitBreakerConfig) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultCircuitBreakerConfig);

        // 이벤트 리스너 등록
        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    CircuitBreaker cb = event.getAddedEntry();
                    cb.getEventPublisher()
                            .onStateTransition(e ->
                                    log.warn("CircuitBreaker '{}' state changed: {} -> {}",
                                            cb.getName(),
                                            e.getStateTransition().getFromState(),
                                            e.getStateTransition().getToState()))
                            .onFailureRateExceeded(e ->
                                    log.error("CircuitBreaker '{}' failure rate exceeded: {}%",
                                            cb.getName(), e.getFailureRate()))
                            .onSlowCallRateExceeded(e ->
                                    log.warn("CircuitBreaker '{}' slow call rate exceeded: {}%",
                                            cb.getName(), e.getSlowCallRate()));
                });

        return registry;
    }

    /**
     * 기본 Retry 설정
     */
    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                // 최대 재시도 횟수
                .maxAttempts(3)
                // 재시도 간격
                .waitDuration(Duration.ofMillis(500))
                // 지수 백오프 사용 (재시도 간격이 점점 증가)
                .intervalFunction(attempt -> Duration.ofMillis(500 * (long) Math.pow(2, attempt - 1)).toMillis())
                // 재시도할 예외
                .retryExceptions(IOException.class, TimeoutException.class)
                // 재시도하지 않을 예외
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig defaultRetryConfig) {
        RetryRegistry registry = RetryRegistry.of(defaultRetryConfig);

        // 이벤트 리스너 등록
        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    Retry retry = event.getAddedEntry();
                    retry.getEventPublisher()
                            .onRetry(e ->
                                    log.warn("Retry '{}' attempt #{} due to: {}",
                                            retry.getName(),
                                            e.getNumberOfRetryAttempts(),
                                            e.getLastThrowable().getMessage()))
                            .onSuccess(e ->
                                    log.debug("Retry '{}' succeeded after {} attempts",
                                            retry.getName(),
                                            e.getNumberOfRetryAttempts()))
                            .onError(e ->
                                    log.error("Retry '{}' failed after {} attempts",
                                            retry.getName(),
                                            e.getNumberOfRetryAttempts()));
                });

        return registry;
    }

    /**
     * 외부 API용 Circuit Breaker (더 관대한 설정)
     */
    @Bean
    public CircuitBreaker externalApiCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .slidingWindowSize(20)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        return registry.circuitBreaker("externalApi", config);
    }

    /**
     * 결제 서비스용 Circuit Breaker (더 엄격한 설정)
     */
    @Bean
    public CircuitBreaker paymentCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .slidingWindowSize(5)
                .waitDurationInOpenState(Duration.ofSeconds(120))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        return registry.circuitBreaker("payment", config);
    }
}
