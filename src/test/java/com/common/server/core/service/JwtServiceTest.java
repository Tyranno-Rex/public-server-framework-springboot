package com.common.server.core.service;

import com.common.server.common.exception.ExpiredTokenException;
import com.common.server.common.exception.InvalidTokenException;
import com.common.server.config.JwtProperties;
import com.common.server.domain.auth.entity.RefreshToken;
import com.common.server.domain.auth.repository.RefreshTokenRepository;
import com.common.server.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * JwtService 단위 테스트
 *
 * @author 정은성
 * @since 2025-01-08
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService 단위 테스트")
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private User testUser;
    private String testSecret;
    private SecretKey testSecretKey;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(UUID.randomUUID().toString())
                .kakaoId("123456789")
                .nickname("테스트유저")
                .email("test@example.com")
                .phoneNumber("01012345678")
                .build();

        // 테스트용 JWT 설정
        testSecret = "test-jwt-secret-key-for-testing-minimum-256-bits-required-for-hs512-algorithm";
        testSecretKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        // JwtProperties Mock 설정 (lenient로 설정하여 사용되지 않는 stub 허용)
        lenient().when(jwtProperties.getSecret()).thenReturn(testSecret);
        lenient().when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L); // 15분
        lenient().when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L); // 7일
        lenient().when(jwtProperties.getIssuer()).thenReturn("ddip-platform-test");
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken_Success() {
        // when
        String accessToken = jwtService.generateAccessToken(testUser);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();

        // 토큰 파싱하여 검증
        Claims claims = Jwts.parser()
                .verifyWith(testSecretKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testUser.getId());
        assertThat(claims.getIssuer()).isEqualTo("ddip-platform-test");
        assertThat(claims.get("kakaoId", String.class)).isEqualTo(testUser.getKakaoId());
        assertThat(claims.get("nickname", String.class)).isEqualTo(testUser.getNickname());
        assertThat(claims.get("email", String.class)).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Refresh Token 생성 및 DB 저장 성공")
    void generateRefreshToken_Success() {
        // given
        RefreshToken savedToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUser.getId())
                .token("generated-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        // when
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();

        // DB 저장 검증
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));

        // 토큰 파싱하여 검증
        Claims claims = Jwts.parser()
                .verifyWith(testSecretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testUser.getId());
        assertThat(claims.getIssuer()).isEqualTo("ddip-platform-test");
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateTokenAndGetUserId_ValidToken_Success() {
        // given
        String validToken = jwtService.generateAccessToken(testUser);

        // when
        String userId = jwtService.validateTokenAndGetUserId(validToken);

        // then
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateTokenAndGetUserId_ExpiredToken_ThrowsException() {
        // given - 만료된 토큰 생성 (만료 시간 -1초)
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000);

        String expiredToken = Jwts.builder()
                .subject(testUser.getId())
                .issuer("ddip-platform-test")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(expiredDate)
                .signWith(testSecretKey)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtService.validateTokenAndGetUserId(expiredToken))
                .isInstanceOf(ExpiredTokenException.class)
                .hasMessageContaining("만료된 토큰입니다");
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 실패")
    void validateTokenAndGetUserId_MalformedToken_ThrowsException() {
        // given
        String malformedToken = "invalid.jwt.token";

        // when & then
        assertThatThrownBy(() -> jwtService.validateTokenAndGetUserId(malformedToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("잘못된 토큰 형식입니다");
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 검증 실패")
    void validateTokenAndGetUserId_InvalidSignature_ThrowsException() {
        // given - 다른 키로 서명된 토큰
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "wrong-secret-key-for-testing-minimum-256-bits-required-for-hs512".getBytes(StandardCharsets.UTF_8)
        );

        String wrongSignedToken = Jwts.builder()
                .subject(testUser.getId())
                .issuer("ddip-platform-test")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(wrongKey)
                .compact();

        // when & then - 서명 오류도 InvalidTokenException으로 처리됨
        assertThatThrownBy(() -> jwtService.validateTokenAndGetUserId(wrongSignedToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신 성공")
    void refreshAccessToken_Success() {
        // given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUser.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(storedToken));

        // when
        String newAccessToken = jwtService.refreshAccessToken(refreshToken, testUser);

        // then
        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).isNotEmpty();

        // 새 토큰이 유효한지 검증
        String userId = jwtService.validateTokenAndGetUserId(newAccessToken);
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("DB에 없는 Refresh Token으로 갱신 실패")
    void refreshAccessToken_TokenNotInDB_ThrowsException() {
        // given
        String refreshToken = jwtService.generateRefreshToken(testUser);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> jwtService.refreshAccessToken(refreshToken, testUser))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다");
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 갱신 실패")
    void refreshAccessToken_ExpiredToken_ThrowsException() {
        // given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUser.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 이미 만료됨
                .build();

        when(refreshTokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> jwtService.refreshAccessToken(refreshToken, testUser))
                .isInstanceOf(ExpiredTokenException.class)
                .hasMessageContaining("만료된 Refresh Token입니다");
    }

    @Test
    @DisplayName("다른 사용자의 Refresh Token으로 갱신 실패")
    void refreshAccessToken_DifferentUser_ThrowsException() {
        // given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId("different-user-id")
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(storedToken));

        // when & then
        assertThatThrownBy(() -> jwtService.refreshAccessToken(refreshToken, testUser))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Refresh Token의 사용자 정보가 일치하지 않습니다");
    }

    @Test
    @DisplayName("모든 Refresh Token 삭제 성공")
    void deleteAllRefreshTokens_Success() {
        // given
        String userId = testUser.getId();

        // when
        jwtService.deleteAllRefreshTokens(userId);

        // then
        verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
    }

    @Test
    @DisplayName("만료된 Refresh Token 일괄 삭제 성공")
    void deleteExpiredRefreshTokens_Success() {
        // when
        jwtService.deleteExpiredRefreshTokens();

        // then
        verify(refreshTokenRepository, times(1))
                .deleteAllByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("유효한 토큰 검증 - isValidToken() 성공")
    void isValidToken_ValidToken_ReturnsTrue() {
        // given
        String validToken = jwtService.generateAccessToken(testUser);

        // when
        boolean isValid = jwtService.isValidToken(validToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 - isValidToken() 실패")
    void isValidToken_InvalidToken_ReturnsFalse() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = jwtService.isValidToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 - isValidToken() 실패")
    void isValidToken_ExpiredToken_ReturnsFalse() {
        // given
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000);

        String expiredToken = Jwts.builder()
                .subject(testUser.getId())
                .issuer("ddip-platform-test")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(expiredDate)
                .signWith(testSecretKey)
                .compact();

        // when
        boolean isValid = jwtService.isValidToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }
}
