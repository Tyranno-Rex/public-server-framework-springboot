package com.common.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 설정 속성
 *
 * CORS(Cross-Origin Resource Sharing) 관련 설정을 application.properties에서 주입받습니다.
 * 환경별로 다른 Origin을 허용할 수 있도록 구성되어 있습니다.
 *
 * application.properties 설정 예시:
 * ```
 * cors.allowed-origins=http://localhost:3000,http://localhost:8081,https://ddip.app
 * cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
 * cors.allowed-headers=*
 * cors.allow-credentials=true
 * cors.max-age=3600
 * ```
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * 허용할 Origin 목록
     *
     * 개발 환경: http://localhost:3000, http://localhost:8081
     * 스테이징 환경: https://staging.ddip.app
     * 프로덕션 환경: https://ddip.app
     */
    private List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:8081"
    );

    /**
     * 허용할 HTTP 메서드
     */
    private List<String> allowedMethods = List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
    );

    /**
     * 허용할 HTTP 헤더
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * 자격 증명 허용 여부
     */
    private Boolean allowCredentials = true;

    /**
     * Preflight 요청 캐시 시간 (초)
     */
    private Long maxAge = 3600L;
}
