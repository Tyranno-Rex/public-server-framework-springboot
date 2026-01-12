package com.common.server.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * HTML 태그 금지 어노테이션
 *
 * XSS 공격 방지를 위해 HTML 태그를 포함하지 않도록 검증합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {

    String message() default "HTML tags are not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
