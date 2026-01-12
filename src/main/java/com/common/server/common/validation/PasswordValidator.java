package com.common.server.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 비밀번호 유효성 검사기
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    @Override
    public void initialize(Password constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null은 @NotNull로 검증
        }

        StringBuilder errorMessage = new StringBuilder();
        boolean valid = true;

        // 길이 검사
        if (value.length() < minLength) {
            valid = false;
            errorMessage.append("Password must be at least ").append(minLength).append(" characters. ");
        }

        if (value.length() > maxLength) {
            valid = false;
            errorMessage.append("Password must be at most ").append(maxLength).append(" characters. ");
        }

        // 대문자 검사
        if (requireUppercase && !value.matches(".*[A-Z].*")) {
            valid = false;
            errorMessage.append("Password must contain at least one uppercase letter. ");
        }

        // 소문자 검사
        if (requireLowercase && !value.matches(".*[a-z].*")) {
            valid = false;
            errorMessage.append("Password must contain at least one lowercase letter. ");
        }

        // 숫자 검사
        if (requireDigit && !value.matches(".*\\d.*")) {
            valid = false;
            errorMessage.append("Password must contain at least one digit. ");
        }

        // 특수문자 검사
        if (requireSpecialChar && !value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            valid = false;
            errorMessage.append("Password must contain at least one special character. ");
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage.toString().trim())
                    .addConstraintViolation();
        }

        return valid;
    }
}
