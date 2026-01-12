package com.common.server.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 전화번호 유효성 검사기
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    // 한국 전화번호 패턴 (휴대폰, 일반전화)
    private static final Pattern KR_PHONE_PATTERN = Pattern.compile(
            "^(01[016789])[.-]?(\\d{3,4})[.-]?(\\d{4})$|" +  // 휴대폰
            "^(0[2-6][1-5]?)[.-]?(\\d{3,4})[.-]?(\\d{4})$"   // 일반전화
    );

    // 국제 전화번호 패턴 (간단한 검증)
    private static final Pattern INTERNATIONAL_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{6,14}$"
    );

    private String region;

    @Override
    public void initialize(Phone constraintAnnotation) {
        this.region = constraintAnnotation.region();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null은 @NotNull로 검증
        }

        // 공백 제거
        String cleaned = value.replaceAll("\\s+", "");

        if ("KR".equalsIgnoreCase(region)) {
            return KR_PHONE_PATTERN.matcher(cleaned).matches();
        }

        return INTERNATIONAL_PATTERN.matcher(cleaned).matches();
    }
}
