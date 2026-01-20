package com.common.server.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 프로퍼티
 *
 * application.properties에서 jwt.* 프로퍼티를 바인딩합니다.
 *
 * <p><strong>보안 주의사항:</strong>
 * JWT Secret은 최소 32자(256비트) 이상이어야 합니다.
 * 애플리케이션 시작 시 자동으로 검증됩니다.</p>
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@Slf4j
public class JwtProperties {

    /**
     * JWT Secret 최소 길이 (256비트 = 32바이트)
     */
    private static final int MIN_SECRET_LENGTH = 32;

    /**
     * JWT 서명에 사용할 시크릿 키
     * 최소 256비트(32자) 이상 필수
     */
    private String secret;

    /**
     * Access Token 만료 시간 (밀리초)
     * 기본값: 15분 (900000ms)
     */
    private Long accessTokenExpiration = 900000L;

    /**
     * Refresh Token 만료 시간 (밀리초)
     * 기본값: 7일 (604800000ms)
     */
    private Long refreshTokenExpiration = 604800000L;

    /**
     * JWT 발급자 (issuer)
     */
    private String issuer = "ddip-platform";

    /**
     * 애플리케이션 시작 시 JWT 설정 검증
     *
     * @throws IllegalStateException JWT Secret이 설정되지 않았거나 길이가 부족한 경우
     */
    @PostConstruct
    public void validateJwtSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT Secret이 설정되지 않았습니다. " +
                    "환경 변수 JWT_SECRET 또는 jwt.secret 프로퍼티를 설정하세요.");
        }

        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    String.format("JWT Secret이 너무 짧습니다. 최소 %d자 이상이어야 합니다. " +
                            "(현재: %d자) 'openssl rand -base64 64' 명령어로 생성하세요.",
                            MIN_SECRET_LENGTH, secret.length()));
        }

        // 약한 시크릿 패턴 감지 (예: "secret", "password", 반복 문자 등)
        if (isWeakSecret(secret)) {
            log.warn("JWT Secret이 약한 패턴으로 감지되었습니다. " +
                    "프로덕션 환경에서는 강력한 랜덤 시크릿을 사용하세요.");
        }

        log.info("JWT 설정 검증 완료 - Secret 길이: {}자, Access Token 만료: {}ms, Refresh Token 만료: {}ms",
                secret.length(), accessTokenExpiration, refreshTokenExpiration);
    }

    /**
     * 약한 시크릿 패턴 감지
     */
    private boolean isWeakSecret(String secret) {
        String lowerSecret = secret.toLowerCase();

        // 일반적인 약한 패턴
        String[] weakPatterns = {
                "secret", "password", "123456", "jwt-secret", "your_",
                "change_me", "test", "default", "sample"
        };

        for (String pattern : weakPatterns) {
            if (lowerSecret.contains(pattern)) {
                return true;
            }
        }

        // 반복 문자 체크 (예: "aaaaaaa...")
        if (secret.chars().distinct().count() < 10) {
            return true;
        }

        return false;
    }
}
