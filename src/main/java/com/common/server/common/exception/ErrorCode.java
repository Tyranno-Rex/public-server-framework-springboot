package com.common.server.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의 (프레임워크 기본 제공)
 *
 * 프로젝트에서 필요에 따라 추가 에러 코드를 정의하여 사용합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (1xxx)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C005", "필수 요청 파라미터가 누락되었습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "리소스를 찾을 수 없습니다."),

    // Authentication Errors (3xxx)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "A003", "Refresh Token을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 Refresh Token입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "A005", "인증이 필요합니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "A006", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
