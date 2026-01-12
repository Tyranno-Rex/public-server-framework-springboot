package com.common.server.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 비밀번호 유효성 검사 어노테이션
 *
 * 안전한 비밀번호 정책을 적용합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {

    String message() default "Password must be at least 8 characters with uppercase, lowercase, number, and special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 최소 길이
     */
    int minLength() default 8;

    /**
     * 최대 길이
     */
    int maxLength() default 100;

    /**
     * 대문자 필수 여부
     */
    boolean requireUppercase() default true;

    /**
     * 소문자 필수 여부
     */
    boolean requireLowercase() default true;

    /**
     * 숫자 필수 여부
     */
    boolean requireDigit() default true;

    /**
     * 특수문자 필수 여부
     */
    boolean requireSpecialChar() default true;
}
