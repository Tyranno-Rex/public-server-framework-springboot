package com.common.server.core.service.interfaces;

import com.common.server.dto.auth.LoginRequestDto;
import com.common.server.dto.auth.LoginResponseDto;
import com.common.server.dto.auth.RefreshTokenResponseDto;

/**
 * 인증 서비스 인터페이스
 *
 * 인증 관련 비즈니스 로직의 계약을 정의합니다.
 * 로그인, JWT 토큰 발급/갱신, 로그아웃 등을 처리합니다.
 *
 * 실제 구현은 프로젝트에서 AuthService를 구현하여 사용합니다.
 */
public interface AuthService {

    /**
     * 로그인
     *
     * @param requestDto 로그인 요청 DTO
     * @return LoginResponseDto (JWT 토큰)
     */
    LoginResponseDto login(LoginRequestDto requestDto);

    /**
     * Refresh Token으로 Access Token 갱신
     *
     * @param refreshToken Refresh Token
     * @return RefreshTokenResponseDto (새로운 Access Token)
     */
    RefreshTokenResponseDto refreshAccessToken(String refreshToken);

    /**
     * 로그아웃
     *
     * @param userId 사용자 ID
     */
    void logout(String userId);
}
