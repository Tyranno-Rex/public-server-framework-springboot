package com.common.server.common.exception;

/**
 * 만료된 토큰 예외
 *
 * @author 정은성
 * @since 2025-01-08
 */
public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }

    public ExpiredTokenException(String message) {
        super(ErrorCode.EXPIRED_TOKEN, message);
    }
}
