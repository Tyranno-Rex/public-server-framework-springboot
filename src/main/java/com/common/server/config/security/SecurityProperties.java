package com.common.server.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 보안 관련 설정 프로퍼티
 *
 * 환경별로 다른 보안 정책을 적용할 수 있습니다.
 * - dev: Swagger 활성화, 디버그 모드
 * - prod: Swagger 비활성화, 보안 강화
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Component
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

    private Swagger swagger = new Swagger();
    private Cors cors = new Cors();
    private Headers headers = new Headers();
    private Debug debug = new Debug();

    @Getter
    @Setter
    public static class Swagger {
        /** Swagger UI 활성화 여부 */
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Cors {
        /** CORS 활성화 여부 */
        private boolean enabled = true;
        /** 모든 Origin 허용 여부 (개발용) */
        private boolean allowAll = false;
    }

    @Getter
    @Setter
    public static class Headers {
        /** X-Frame-Options 헤더 활성화 */
        private boolean frameOptions = true;
        /** X-Content-Type-Options 헤더 활성화 */
        private boolean contentTypeOptions = true;
        /** X-XSS-Protection 헤더 활성화 */
        private boolean xssProtection = true;
        /** Strict-Transport-Security 헤더 활성화 (HTTPS) */
        private boolean hsts = false;
    }

    @Getter
    @Setter
    public static class Debug {
        /** 상세 에러 메시지 노출 여부 */
        private boolean exposeErrors = false;
        /** 스택 트레이스 노출 여부 */
        private boolean exposeStackTrace = false;
    }
}
