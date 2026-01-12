package com.common.server.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Custom Validation Annotations 테스트")
class ValidationIntegrationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("@Phone 검증")
    class PhoneValidationTest {

        record PhoneTestDto(@Phone String phone) {}

        @Test
        @DisplayName("유효한 한국 휴대폰 번호")
        void validKoreanMobileNumber() {
            // given
            PhoneTestDto dto = new PhoneTestDto("010-1234-5678");

            // when
            Set<ConstraintViolation<PhoneTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("유효한 국제 형식 번호")
        void validInternationalNumber() {
            // given
            PhoneTestDto dto = new PhoneTestDto("+82-10-1234-5678");

            // when
            Set<ConstraintViolation<PhoneTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("잘못된 형식")
        void invalidFormat() {
            // given
            PhoneTestDto dto = new PhoneTestDto("1234");

            // when
            Set<ConstraintViolation<PhoneTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("유효한 전화번호 형식이 아닙니다.");
        }

        @Test
        @DisplayName("null 허용")
        void nullAllowed() {
            // given
            PhoneTestDto dto = new PhoneTestDto(null);

            // when
            Set<ConstraintViolation<PhoneTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("@NoHtml 검증")
    class NoHtmlValidationTest {

        record HtmlTestDto(@NoHtml String content) {}

        @Test
        @DisplayName("일반 텍스트 허용")
        void plainTextAllowed() {
            // given
            HtmlTestDto dto = new HtmlTestDto("Hello, World!");

            // when
            Set<ConstraintViolation<HtmlTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("HTML 태그 포함 거부")
        void htmlTagRejected() {
            // given
            HtmlTestDto dto = new HtmlTestDto("<script>alert('xss')</script>");

            // when
            Set<ConstraintViolation<HtmlTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("HTML 태그를 포함할 수 없습니다.");
        }

        @Test
        @DisplayName("이벤트 핸들러 거부")
        void eventHandlerRejected() {
            // given
            HtmlTestDto dto = new HtmlTestDto("onclick=alert('xss')");

            // when
            Set<ConstraintViolation<HtmlTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    @DisplayName("@SafeString 검증")
    class SafeStringValidationTest {

        record SafeStringTestDto(@SafeString String text) {}

        @Test
        @DisplayName("안전한 문자열 허용")
        void safeStringAllowed() {
            // given
            SafeStringTestDto dto = new SafeStringTestDto("Hello World 123");

            // when
            Set<ConstraintViolation<SafeStringTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("SQL 인젝션 패턴 거부")
        void sqlInjectionRejected() {
            // given
            SafeStringTestDto dto = new SafeStringTestDto("'; DROP TABLE users; --");

            // when
            Set<ConstraintViolation<SafeStringTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("스크립트 태그 거부")
        void scriptTagRejected() {
            // given
            SafeStringTestDto dto = new SafeStringTestDto("<script>alert(1)</script>");

            // when
            Set<ConstraintViolation<SafeStringTestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
        }
    }
}
