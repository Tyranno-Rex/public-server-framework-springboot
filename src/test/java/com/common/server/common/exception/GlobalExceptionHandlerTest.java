package com.common.server.common.exception;

import com.common.server.dto.common.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler 단위 테스트
 */
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Nested
    @DisplayName("BusinessException 처리")
    class HandleBusinessException {

        @Test
        @DisplayName("INVALID_INPUT_VALUE 에러 처리")
        void handleBusinessException_InvalidInput() {
            // given
            BusinessException exception = new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

            // when
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("C001");
            assertThat(response.getBody().getMessage()).isEqualTo("잘못된 입력값입니다.");
        }

        @Test
        @DisplayName("UNAUTHORIZED_ACCESS 에러 처리")
        void handleBusinessException_Unauthorized() {
            // given
            BusinessException exception = new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);

            // when
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("A005");
        }

        @Test
        @DisplayName("FORBIDDEN_ACCESS 에러 처리")
        void handleBusinessException_Forbidden() {
            // given
            BusinessException exception = new BusinessException(ErrorCode.FORBIDDEN_ACCESS);

            // when
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("A006");
        }
    }

    @Nested
    @DisplayName("EntityNotFoundException 처리")
    class HandleEntityNotFoundException {

        @Test
        @DisplayName("엔티티 찾기 실패 에러 처리 - BusinessException으로 처리됨")
        void handleEntityNotFoundException() {
            // given - EntityNotFoundException은 BusinessException을 상속하므로 handleBusinessException으로 처리
            EntityNotFoundException exception = new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "User를 찾을 수 없습니다.");

            // when
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("C006");
            assertThat(response.getBody().getMessage()).isEqualTo("User를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException 처리")
    class HandleValidationException {

        @Test
        @DisplayName("유효성 검증 실패 에러 처리")
        void handleValidationException() throws NoSuchMethodException {
            // given
            TestDto testDto = new TestDto();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testDto, "testDto");
            bindingResult.addError(new FieldError("testDto", "name", null, false, null, null, "이름은 필수입니다"));
            bindingResult.addError(new FieldError("testDto", "email", "invalid", false, null, null, "올바른 이메일 형식이 아닙니다"));

            MethodParameter methodParameter = new MethodParameter(
                    this.getClass().getDeclaredMethod("handleValidationException"),
                    -1
            );

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

            // when
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("C001");
            assertThat(response.getBody().getErrors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("일반 Exception 처리")
    class HandleGeneralException {

        @Test
        @DisplayName("예상치 못한 에러 처리")
        void handleGeneralException() {
            // given
            Exception exception = new RuntimeException("Unexpected error");

            // when
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("C003");
        }
    }

    // 테스트용 DTO
    @SuppressWarnings("unused")
    static class TestDto {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
