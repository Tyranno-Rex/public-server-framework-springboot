package com.common.server.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 *
 * JWT Access Token, Refresh Token을 반환합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    /**
     * JWT Access Token
     */
    private String accessToken;

    /**
     * JWT Refresh Token
     */
    private String refreshToken;

    /**
     * 토큰 타입 (Bearer)
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access Token 만료 시간 (초)
     */
    private Long expiresIn;
}
