package com.common.server.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 *
 * Swagger UI를 통해 API 문서를 자동 생성하고 테스트할 수 있도록 설정합니다.
 *
 * 접속 URL:
 * - Swagger UI: http://localhost:8080/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 정의
        String jwtSchemeName = "bearerAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."));

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.ddip.com")
                                .description("프로덕션 서버 (예정)")
                ))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("DDIP API")
                .description("""
                        DDIP (따딥) 소개팅 플랫폼 REST API 문서

                        ## 인증 방법
                        1. POST /api/auth/login 으로 카카오 Access Token을 전송하여 JWT 토큰 발급
                        2. 발급받은 Access Token을 Authorization 헤더에 'Bearer {token}' 형식으로 포함
                        3. 토큰 만료 시 POST /api/auth/refresh 로 새로운 Access Token 발급

                        ## 주요 기능
                        - 카카오 로그인 기반 인증
                        - JWT Access Token (15분) 및 Refresh Token (7일)
                        - 사용자 프로필 관리
                        - 게시글 CRUD
                        - 댓글 시스템

                        ## 에러 응답 형식
                        모든 에러는 다음과 같은 형식으로 반환됩니다:
                        ```json
                        {
                          "status": 400,
                          "code": "C001",
                          "message": "에러 메시지",
                          "timestamp": "2025-01-08 12:00:00",
                          "path": "/api/auth/login"
                        }
                        ```
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("DDIP Team")
                        .email("contact@ddip.com")
                        .url("https://ddip.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
