package com.common.server.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

/**
 * Spring Security 설정
 *
 * JWT 기반 Stateless 인증을 사용합니다.
 * 프로젝트에서 필요에 따라 커스터마이징하여 사용합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityProperties securityProperties;

    /**
     * RestTemplate Bean 등록
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("=== Security Configuration ===");
        log.info("Swagger enabled: {}", securityProperties.getSwagger().isEnabled());
        log.info("Debug expose errors: {}", securityProperties.getDebug().isExposeErrors());
        log.info("HSTS enabled: {}", securityProperties.getHeaders().isHsts());
        log.info("==============================");

        http
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf(csrf -> csrf.disable())

            // CORS 설정
            .cors(cors -> cors.configure(http))

            // 세션 사용 안 함 (JWT Stateless 인증)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 보안 헤더 설정
            .headers(headers -> {
                // X-Frame-Options
                if (securityProperties.getHeaders().isFrameOptions()) {
                    headers.frameOptions(frame -> frame.deny());
                }

                // X-Content-Type-Options
                if (securityProperties.getHeaders().isContentTypeOptions()) {
                    headers.contentTypeOptions(content -> {});
                }

                // X-XSS-Protection (최신 브라우저는 자체 XSS 필터 사용)
                if (securityProperties.getHeaders().isXssProtection()) {
                    headers.xssProtection(xss -> xss.disable());
                }

                // HSTS (HTTPS 환경에서만)
                if (securityProperties.getHeaders().isHsts()) {
                    headers.httpStrictTransportSecurity(hsts ->
                        hsts.includeSubDomains(true)
                            .maxAgeInSeconds(31536000) // 1년
                    );
                }
            })

            // 엔드포인트별 인증 설정
            .authorizeHttpRequests(authz -> {
                // 인증 없이 접근 가능한 경로
                authz.requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/register").permitAll()
                    .requestMatchers("/api/auth/refresh").permitAll()
                    .requestMatchers("/api/auth/health").permitAll()
                    .requestMatchers("/api/health/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll();

                // Swagger: 환경 설정에 따라
                if (securityProperties.getSwagger().isEnabled()) {
                    authz.requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll();
                    log.info("Swagger UI: ENABLED");
                }

                // 나머지는 모두 인증 필요
                authz.anyRequest().authenticated();
            })

            // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
