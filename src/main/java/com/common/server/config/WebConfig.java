package com.common.server.config;

import com.common.server.common.logging.LoggingInterceptor;
import com.common.server.common.ratelimit.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정
 *
 * CORS 설정을 CorsProperties에서 주입받아 적용합니다.
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                .allowCredentials(corsProperties.getAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로깅 인터셉터 (가장 먼저 실행)
        registry.addInterceptor(loggingInterceptor)
                .order(1)
                .addPathPatterns("/api/**");

        // Rate Limiting 인터셉터
        registry.addInterceptor(rateLimitInterceptor)
                .order(2)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/health", "/api/health/**");
    }
} 