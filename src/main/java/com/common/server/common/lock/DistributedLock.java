package com.common.server.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락 어노테이션
 *
 * 메서드에 적용하여 분산 환경에서 동시성 제어를 수행합니다.
 * Redis를 사용하여 락을 구현합니다.
 *
 * 사용 예시:
 * <pre>
 * {@literal @}DistributedLock(key = "'order:' + #orderId", waitTime = 5, leaseTime = 10)
 * public void processOrder(String orderId) {
 *     // 동시 실행 방지
 * }
 * </pre>
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키 (SpEL 표현식 지원)
     * 예: "'user:' + #userId" 또는 "'order:' + #order.id"
     */
    String key();

    /**
     * 락 대기 시간 (기본: 5초)
     */
    long waitTime() default 5;

    /**
     * 락 유지 시간 (기본: 10초)
     * 이 시간이 지나면 자동으로 락 해제
     */
    long leaseTime() default 10;

    /**
     * 시간 단위 (기본: 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 락 획득 실패 시 예외 발생 여부
     * false면 메서드 실행 없이 null 반환
     */
    boolean throwOnFailure() default true;
}
