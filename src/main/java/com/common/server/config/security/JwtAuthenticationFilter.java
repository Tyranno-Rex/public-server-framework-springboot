package com.common.server.config.security;

import com.common.server.core.service.interfaces.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 인증 필터
 *
 * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출하고 검증하여
 * Spring Security의 SecurityContext에 인증 정보를 설정합니다.
 *
 * 동작 흐름:
 * 1. Authorization 헤더에서 Bearer 토큰 추출
 * 2. JWT 토큰 검증 및 사용자 ID 추출
 * 3. SecurityContext에 인증 정보 저장
 * 4. 다음 필터로 요청 전달
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. JWT 토큰이 존재하고 유효한 경우
            if (StringUtils.hasText(jwt)) {
                try {
                    // 3. JWT 토큰 검증 및 사용자 ID 추출
                    String userId = jwtService.validateTokenAndGetUserId(jwt);

                    // 4. Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    new ArrayList<>()  // 권한 목록 (현재는 빈 리스트)
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 5. SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT 인증 성공: userId={}, path={}", userId, request.getRequestURI());

                } catch (RuntimeException e) {
                    log.warn("JWT 인증 실패: {}, path={}", e.getMessage(), request.getRequestURI());
                    // 인증 실패 시에도 필터 체인 계속 진행 (Spring Security가 처리)
                }
            }

        } catch (Exception e) {
            log.error("JWT 필터에서 예외 발생: {}", e.getMessage(), e);
        }

        // 6. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     *
     * Authorization 헤더에서 "Bearer " 접두사를 제거하고 순수 토큰만 반환
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 특정 경로는 JWT 필터를 건너뜀
     *
     * @param request HTTP 요청
     * @return 필터를 건너뛸지 여부
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 인증이 필요 없는 경로
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs");
    }
}
