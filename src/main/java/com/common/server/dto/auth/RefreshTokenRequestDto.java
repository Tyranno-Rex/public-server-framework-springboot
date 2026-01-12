package com.common.server.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 요청 DTO
 *
 * Refresh Token으로 새로운 Access Token을 발급받습니다.
 *
 * @author DDIP Team
 * @since 2025-01-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {

    /**
     * Refresh Token
     */
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
