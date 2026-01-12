package com.common.server.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * HTML 태그 금지 검사기
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return !HTML_PATTERN.matcher(value).find();
    }
}
