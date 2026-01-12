package com.common.server.common.exception;

/**
 * 유효하지 않은 토큰 예외
 *
 * @author 정은성
 * @since 2025-01-08
 */
public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

    public InvalidTokenException(Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, cause);
    }
}
