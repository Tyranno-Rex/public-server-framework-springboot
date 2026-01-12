package com.common.server.common.exception;

/**
 * 엔티티를 찾을 수 없을 때 발생하는 예외
 *
 * @author 정은성
 * @since 2025-01-08
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
