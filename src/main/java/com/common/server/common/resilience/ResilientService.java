package com.common.server.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Resilience 헬퍼 서비스
 *
 * Circuit Breaker와 Retry 패턴을 쉽게 사용할 수 있게 해주는 서비스입니다.
 *
 * 사용 예시:
 * <pre>
 * String result = resilientService.executeWithResilience(
 *     "externalApi",
 *     () -> externalApiClient.call(),
 *     () -> "fallback value"
 * );
 * </pre>
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResilientService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    /**
     * Circuit Breaker + Retry를 적용하여 실행
     *
     * @param name Circuit Breaker/Retry 이름
     * @param supplier 실행할 작업
     * @param fallback 실패 시 대체 값 제공
     * @return 실행 결과 또는 fallback 값
     */
    public <T> T executeWithResilience(String name, Supplier<T> supplier, Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        Retry retry = retryRegistry.retry(name);

        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            log.error("Resilient execution failed for '{}', using fallback: {}", name, e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Circuit Breaker만 적용하여 실행
     */
    public <T> T executeWithCircuitBreaker(String name, Supplier<T> supplier, Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);

        try {
            return circuitBreaker.executeSupplier(supplier);
        } catch (Exception e) {
            log.error("Circuit breaker execution failed for '{}', using fallback: {}", name, e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Retry만 적용하여 실행
     */
    public <T> T executeWithRetry(String name, Supplier<T> supplier) {
        Retry retry = retryRegistry.retry(name);
        return retry.executeSupplier(supplier);
    }

    /**
     * Circuit Breaker + Retry를 적용하여 실행 (Runnable)
     */
    public void executeWithResilience(String name, Runnable runnable, Runnable fallback) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        Retry retry = retryRegistry.retry(name);

        Runnable decoratedRunnable = CircuitBreaker.decorateRunnable(circuitBreaker, runnable);
        decoratedRunnable = Retry.decorateRunnable(retry, decoratedRunnable);

        try {
            decoratedRunnable.run();
        } catch (Exception e) {
            log.error("Resilient execution failed for '{}', using fallback: {}", name, e.getMessage());
            fallback.run();
        }
    }

    /**
     * Circuit Breaker 상태 조회
     */
    public CircuitBreaker.State getCircuitBreakerState(String name) {
        return circuitBreakerRegistry.circuitBreaker(name).getState();
    }

    /**
     * Circuit Breaker 상태 초기화
     */
    public void resetCircuitBreaker(String name) {
        circuitBreakerRegistry.circuitBreaker(name).reset();
        log.info("Circuit breaker '{}' has been reset", name);
    }
}
