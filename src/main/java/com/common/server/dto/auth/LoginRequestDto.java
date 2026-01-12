package com.common.server.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO (기본 템플릿)
 *
 * 프로젝트에서 필요에 따라 필드를 추가/수정하여 사용합니다.
 * 예: OAuth 토큰, 아이디/비밀번호 등
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    /**
     * 인증 토큰 (OAuth Access Token 등)
     */
    @NotBlank(message = "인증 토큰은 필수입니다.")
    private String token;

    /**
     * 인증 제공자 (kakao, google, apple 등)
     */
    private String provider;
}
