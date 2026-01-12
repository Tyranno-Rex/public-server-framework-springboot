package com.common.server.common.exception;

import com.common.server.dto.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 전역 예외 핸들러
 *
 * 애플리케이션에서 발생하는 모든 예외를 처리하여 표준 에러 응답을 반환합니다.
 *
 * @author 정은성
 * @since 2025-01-08
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        log.error("BusinessException: code={}, message={}, path={}",
                e.getErrorCode().getCode(), e.getMessage(), request.getRequestURI(), e);

        ErrorResponse response = ErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value())
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, e.getErrorCode().getStatus());
    }

    /**
     * @Valid 유효성 검증 실패 (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        log.error("MethodArgumentNotValidException: path={}", request.getRequestURI(), e);

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(fieldError.getField())
                    .value(fieldError.getRejectedValue() != null ?
                           fieldError.getRejectedValue().toString() : null)
                    .reason(fieldError.getDefaultMessage())
                    .build());
        }

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @ModelAttribute 유효성 검증 실패 (BindException)
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(
            BindException e,
            HttpServletRequest request
    ) {
        log.error("BindException: path={}", request.getRequestURI(), e);

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(fieldError.getField())
                    .value(fieldError.getRejectedValue() != null ?
                           fieldError.getRejectedValue().toString() : null)
                    .reason(fieldError.getDefaultMessage())
                    .build());
        }

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 타입 불일치 (MethodArgumentTypeMismatchException)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        log.error("MethodArgumentTypeMismatchException: path={}", request.getRequestURI(), e);

        String message = String.format("'%s' 파라미터의 타입이 잘못되었습니다. 기대값: %s, 입력값: %s",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown",
                e.getValue());

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_TYPE_VALUE.getCode())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 필수 파라미터 누락 (MissingServletRequestParameterException)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request
    ) {
        log.error("MissingServletRequestParameterException: path={}", request.getRequestURI(), e);

        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.MISSING_REQUEST_PARAMETER.getCode())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 허용되지 않은 HTTP 메서드 (HttpRequestMethodNotSupportedException)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        log.error("HttpRequestMethodNotSupportedException: path={}", request.getRequestURI(), e);

        String message = String.format("'%s' 메서드는 지원하지 않습니다. 지원 메서드: %s",
                e.getMethod(), String.join(", ", e.getSupportedMethods() != null ?
                e.getSupportedMethods() : new String[]{}));

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .code(ErrorCode.METHOD_NOT_ALLOWED.getCode())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 기타 모든 예외 (Exception)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unexpected Exception: path={}", request.getRequestURI(), e);

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
