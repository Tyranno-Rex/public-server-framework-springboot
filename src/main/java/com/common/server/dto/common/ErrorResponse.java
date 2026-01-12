package com.common.server.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 표준 에러 응답 DTO
 *
 * 모든 API 에러 응답에 사용되는 표준 형식입니다.
 *
 * @author DDIP Team
 * @since 2025-01-08
 */
@Getter
@Builder
public class ErrorResponse {

    /**
     * HTTP 상태 코드
     */
    private final int status;

    /**
     * 에러 코드 (예: USER_NOT_FOUND, INVALID_TOKEN)
     */
    private final String code;

    /**
     * 에러 메시지
     */
    private final String message;

    /**
     * 에러 발생 시각
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * 요청 경로
     */
    private final String path;

    /**
     * 상세 에러 목록 (유효성 검증 실패 시)
     */
    private final List<FieldError> errors;

    /**
     * 필드별 에러 정보
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;
    }
}
