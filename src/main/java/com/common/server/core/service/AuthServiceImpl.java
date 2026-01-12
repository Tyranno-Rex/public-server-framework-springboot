package com.common.server.core.service;

import com.common.server.core.service.interfaces.AuthService;
import com.common.server.core.service.interfaces.JwtService;
import com.common.server.dto.auth.LoginRequestDto;
import com.common.server.dto.auth.LoginResponseDto;
import com.common.server.dto.auth.RefreshTokenResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 기본 구현체 (샘플)
 *
 * 실제 프로젝트에서는 이 클래스를 확장하거나 새로운 AuthService 구현체를 만들어 사용합니다.
 * 사용자 저장소(UserRepository) 등은 프로젝트에서 정의해야 합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;

    /**
     * 로그인 (샘플 구현)
     *
     * 실제 프로젝트에서는 이 메서드를 오버라이드하여 사용자 인증 로직을 구현합니다.
     */
    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // TODO: 프로젝트에서 실제 로그인 로직 구현
        // 1. 사용자 인증 (예: OAuth, 아이디/비밀번호 등)
        // 2. 사용자 조회/생성
        // 3. JWT 토큰 발급

        log.warn("AuthServiceImpl.login()은 샘플 구현입니다. 실제 프로젝트에서 오버라이드하세요.");

        throw new UnsupportedOperationException(
            "AuthService.login()을 프로젝트에서 구현해야 합니다."
        );
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponseDto refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 검증 및 사용자 ID 추출
        String userId = jwtService.validateTokenAndGetUserId(refreshToken);

        // 2. 새로운 Access Token 생성
        String newAccessToken = jwtService.generateAccessTokenById(userId);

        log.info("Access Token 갱신 완료: userId={}", userId);

        return RefreshTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    /**
     * 로그아웃
     */
    @Override
    @Transactional
    public void logout(String userId) {
        jwtService.deleteAllRefreshTokens(userId);
        log.info("로그아웃 완료: userId={}", userId);
    }
}
