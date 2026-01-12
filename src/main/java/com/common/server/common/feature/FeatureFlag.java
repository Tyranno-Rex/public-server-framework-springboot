package com.common.server.common.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Feature Flag 어노테이션
 *
 * 메서드에 적용하여 Feature Flag가 활성화된 경우에만 실행되도록 합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureFlag {

    /**
     * Feature Flag 이름
     */
    String value();

    /**
     * Feature가 비활성화된 경우 예외를 던질지 여부
     * true: FeatureDisabledException 발생
     * false: null 반환
     */
    boolean throwIfDisabled() default true;
}
