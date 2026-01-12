package com.common.server.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 감사 로그 어노테이션
 *
 * 메서드에 적용하여 해당 작업의 감사 로그를 자동으로 기록합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 감사 로그 액션 타입
     */
    String action();

    /**
     * 리소스 타입 (예: USER, ORDER, PRODUCT 등)
     */
    String resource() default "";

    /**
     * 추가 설명
     */
    String description() default "";
}
