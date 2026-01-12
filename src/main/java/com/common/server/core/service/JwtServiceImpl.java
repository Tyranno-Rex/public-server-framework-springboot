package com.common.server.core.service;

import com.common.server.common.exception.ExpiredTokenException;
import com.common.server.common.exception.InvalidTokenException;
import com.common.server.config.JwtProperties;
import com.common.server.core.service.interfaces.JwtService;
import com.common.server.domain.auth.entity.RefreshToken;
import com.common.server.domain.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 서비스 구현체
 *
 * JWT Access Token 및 Refresh Token 생성, 검증, 갱신 기능을 제공합니다.
 *
 * 주요 기능:
 * - Access Token 생성 (기본 15분 만료)
 * - Refresh Token 생성 (기본 7일 만료)
 * - 토큰 검증 및 사용자 ID 추출
 * - Refresh Token으로 Access Token 갱신
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * JWT 서명 키 생성
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성 (사용자 ID만)
     */
    @Override
    public String generateAccessTokenById(String userId) {
        return generateAccessToken(userId, new HashMap<>());
    }

    /**
     * Access Token 생성 (커스텀 클레임 포함)
     */
    @Override
    public String generateAccessToken(String userId, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        var builder = Jwts.builder()
                .subject(userId)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate);

        // 커스텀 클레임 추가
        claims.forEach(builder::claim);

        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * Refresh Token 생성 및 DB 저장
     */
    @Override
    @Transactional
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        String token = Jwts.builder()
                .subject(userId)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        // DB에 Refresh Token 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token 생성 및 저장 완료: userId={}", userId);

        return token;
    }

    /**
     * JWT 토큰 검증 및 사용자 ID 추출
     */
    @Override
    public String validateTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new ExpiredTokenException("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            throw new InvalidTokenException("지원되지 않는 토큰 형식입니다.");
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            throw new InvalidTokenException("잘못된 토큰 형식입니다.");
        } catch (SignatureException e) {
            log.warn("서명이 유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        } catch (SecurityException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    @Override
    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        String userId = validateTokenAndGetUserId(refreshToken);

        // DB에서 Refresh Token 조회
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 Refresh Token입니다."));

        // 토큰 만료 확인
        if (storedToken.isExpired()) {
            log.warn("만료된 Refresh Token: userId={}", userId);
            throw new ExpiredTokenException("만료된 Refresh Token입니다.");
        }

        // 사용자 ID 일치 확인
        if (!storedToken.getUserId().equals(userId)) {
            log.warn("Refresh Token의 사용자 ID 불일치: expected={}, actual={}", userId, storedToken.getUserId());
            throw new InvalidTokenException("Refresh Token의 사용자 정보가 일치하지 않습니다.");
        }

        log.info("Access Token 갱신 완료: userId={}", userId);
        return generateAccessTokenById(userId);
    }

    /**
     * 사용자의 모든 Refresh Token 삭제 (로그아웃)
     */
    @Override
    @Transactional
    public void deleteAllRefreshTokens(String userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
        log.info("모든 Refresh Token 삭제 완료: userId={}", userId);
    }

    /**
     * 만료된 Refresh Token 일괄 삭제 (배치 작업용)
     */
    @Override
    @Transactional
    public void deleteExpiredRefreshTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        log.info("만료된 Refresh Token 삭제 완료");
    }

    /**
     * JWT 토큰 유효성 검증 (true/false 반환)
     */
    @Override
    public boolean isValidToken(String token) {
        try {
            validateTokenAndGetUserId(token);
            return true;
        } catch (ExpiredTokenException | InvalidTokenException e) {
            return false;
        }
    }
}
