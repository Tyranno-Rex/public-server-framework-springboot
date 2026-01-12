package com.common.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 프로퍼티
 *
 * application.properties에서 jwt.* 프로퍼티를 바인딩합니다.
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT 서명에 사용할 시크릿 키
     * 최소 256비트(32자) 이상 권장
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
}
