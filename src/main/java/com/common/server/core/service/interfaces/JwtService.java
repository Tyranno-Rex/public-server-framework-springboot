package com.common.server.core.service.interfaces;

import java.util.Map;

/**
 * JWT 서비스 인터페이스
 *
 * JWT 토큰 생성, 검증, 갱신 관련 계약을 정의합니다.
 */
public interface JwtService {

    /**
     * Access Token 생성 (사용자 ID 기반)
     *
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    String generateAccessTokenById(String userId);

    /**
     * Access Token 생성 (커스텀 클레임 포함)
     *
     * @param userId 사용자 ID
     * @param claims 추가 클레임
     * @return JWT Access Token
     */
    String generateAccessToken(String userId, Map<String, Object> claims);

    /**
     * Refresh Token 생성 및 DB 저장
     *
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    String generateRefreshToken(String userId);

    /**
     * JWT 토큰 검증 및 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    String validateTokenAndGetUserId(String token);

    /**
     * Refresh Token으로 Access Token 갱신
     *
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token
     */
    String refreshAccessToken(String refreshToken);

    /**
     * 사용자의 모든 Refresh Token 삭제 (로그아웃)
     *
     * @param userId 사용자 ID
     */
    void deleteAllRefreshTokens(String userId);

    /**
     * 만료된 Refresh Token 일괄 삭제 (배치 작업용)
     */
    void deleteExpiredRefreshTokens();

    /**
     * JWT 토큰 유효성 검증 (true/false 반환)
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    boolean isValidToken(String token);
}
