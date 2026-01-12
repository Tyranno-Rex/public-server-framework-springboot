package com.common.server.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 응답 DTO
 *
 * 갱신된 Access Token을 반환합니다.
 *
 * @author DDIP Team
 * @since 2025-01-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponseDto {

    /**
     * 새로운 JWT Access Token (15분 만료)
     */
    private String accessToken;

    /**
     * 토큰 타입 (Bearer)
     */
    @Builder.Default
    private String tokenType = "Bearer";
}
