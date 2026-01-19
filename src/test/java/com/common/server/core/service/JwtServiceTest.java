package com.common.server.core.service;

import com.common.server.common.exception.BusinessException;
import com.common.server.config.JwtProperties;
import com.common.server.core.service.interfaces.JwtService;
import com.common.server.domain.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService 테스트")
class JwtServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hs256");
        jwtProperties.setAccessTokenExpiration(900000L); // 15분 (밀리초)
        jwtProperties.setRefreshTokenExpiration(604800000L); // 7일 (밀리초)

        jwtService = new JwtServiceImpl(jwtProperties, refreshTokenRepository);
    }

    @Nested
    @DisplayName("Access Token 생성")
    class GenerateAccessToken {

        @Test
        @DisplayName("유효한 사용자 ID로 Access Token 생성 성공")
        void generateAccessToken_Success() {
            // given
            String userId = "test-user-123";

            // when
            String token = jwtService.generateAccessTokenById(userId);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
        }

        @Test
        @DisplayName("생성된 Access Token에서 사용자 ID 추출 성공")
        void generateAccessToken_ExtractUserId() {
            // given
            String userId = "test-user-123";

            // when
            String token = jwtService.generateAccessTokenById(userId);
            String extractedUserId = jwtService.validateTokenAndGetUserId(token);

            // then
            assertThat(extractedUserId).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    class GenerateRefreshToken {

        @Test
        @DisplayName("유효한 사용자 ID로 Refresh Token 생성 및 저장 성공")
        void generateRefreshToken_Success() {
            // given
            String userId = "test-user-123";

            // when
            String token = jwtService.generateRefreshToken(userId);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            verify(refreshTokenRepository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("Token 검증")
    class ValidateToken {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validateToken_Success() {
            // given
            String userId = "test-user-123";
            String token = jwtService.generateAccessTokenById(userId);

            // when
            String extractedUserId = jwtService.validateTokenAndGetUserId(token);

            // then
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 실패")
        void validateToken_InvalidFormat() {
            // given
            String invalidToken = "invalid-token";

            // when & then
            assertThatThrownBy(() -> jwtService.validateTokenAndGetUserId(invalidToken))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("변조된 토큰 검증 실패")
        void validateToken_TamperedToken() {
            // given
            String userId = "test-user-123";
            String token = jwtService.generateAccessTokenById(userId);
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            // when & then
            assertThatThrownBy(() -> jwtService.validateTokenAndGetUserId(tamperedToken))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Refresh Token 삭제")
    class DeleteRefreshToken {

        @Test
        @DisplayName("사용자의 모든 Refresh Token 삭제 성공")
        void deleteAllRefreshTokens_Success() {
            // given
            String userId = "test-user-123";

            // when
            jwtService.deleteAllRefreshTokens(userId);

            // then
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
        }
    }
}
