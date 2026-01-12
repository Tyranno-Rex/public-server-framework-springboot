package com.common.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 버저닝 설정
 *
 * 패키지 기반으로 API 버전을 자동 적용합니다.
 * - com.common.server.api.v1.* → /api/v1/**
 * - com.common.server.api.v2.* → /api/v2/**
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // v1 패키지의 컨트롤러에 /api/v1 prefix 자동 적용
        configurer.addPathPrefix("/api/v1",
                c -> c.getPackageName().startsWith("com.common.server.api.v1"));

        // v2 패키지의 컨트롤러에 /api/v2 prefix 자동 적용 (미래 확장용)
        configurer.addPathPrefix("/api/v2",
                c -> c.getPackageName().startsWith("com.common.server.api.v2"));
    }
}
