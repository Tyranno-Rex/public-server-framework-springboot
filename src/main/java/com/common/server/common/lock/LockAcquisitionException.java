package com.common.server.common.lock;

/**
 * 락 획득 실패 예외
 *
 * 분산 락을 획득하지 못한 경우 발생하는 예외입니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class LockAcquisitionException extends RuntimeException {

    public LockAcquisitionException(String message) {
        super(message);
    }

    public LockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
