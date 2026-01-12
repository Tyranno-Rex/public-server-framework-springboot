package com.common.server.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 보안 관련 설정 프로퍼티
 *
 * 환경별로 다른 보안 정책을 적용할 수 있습니다.
 * - dev: Swagger 활성화
 * - prod: Swagger 비활성화
 */
@Component
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

    private Swagger swagger = new Swagger();

    @Getter
    @Setter
    public static class Swagger {
        /** Swagger UI 활성화 여부 */
        private boolean enabled = true;
    }
}
