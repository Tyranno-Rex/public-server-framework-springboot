package com.common.server.grpc.interceptor;

import com.common.server.core.service.interfaces.JwtService;
import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * gRPC 인증 인터셉터
 *
 * JWT 토큰을 검증하고 사용자 정보를 Context에 저장
 * 환경 설정(grpc.auth.skip)에 따라 인증 스킵 가능
 *
 * <p><strong>보안 주의사항:</strong>
 * grpc.auth.skip=true 설정은 dev, local 프로파일에서만 허용됩니다.
 * 프로덕션 환경에서는 이 설정이 무시됩니다.</p>
 *
 * @author DDIP Team
 * @since 2025-01-13
 */
@GrpcGlobalServerInterceptor
public class GrpcAuthInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcAuthInterceptor.class);

    // 인증 스킵이 허용되는 프로파일
    private static final String[] AUTH_SKIP_ALLOWED_PROFILES = {"dev", "local", "test"};

    // Metadata 키 정의
    public static final Metadata.Key<String> AUTHORIZATION_KEY =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    // Context 키 - 인증된 사용자 ID 저장
    public static final Context.Key<String> USER_ID_KEY = Context.key("userId");

    // 인증이 필요없는 메서드 목록
    private static final String[] PUBLIC_METHODS = {
        "grpc.health.v1.Health",  // gRPC 헬스체크
        "grpc.reflection.v1alpha.ServerReflection",  // gRPC 리플렉션
    };

    private final JwtService jwtService;
    private final Environment environment;
    private final boolean skipAuthAllowed;

    @Value("${grpc.auth.skip:false}")
    private boolean skipAuth;

    public GrpcAuthInterceptor(JwtService jwtService, Environment environment) {
        this.jwtService = jwtService;
        this.environment = environment;
        this.skipAuthAllowed = isAuthSkipAllowedProfile();

        if (skipAuthAllowed) {
            log.info("gRPC auth skip is ALLOWED in current profile(s): {}",
                    Arrays.toString(environment.getActiveProfiles()));
        } else {
            log.info("gRPC auth skip is DISABLED - production mode");
        }
    }

    /**
     * 현재 프로파일이 인증 스킵이 허용되는 프로파일인지 확인
     */
    private boolean isAuthSkipAllowedProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            // 프로파일이 없으면 기본적으로 보안 모드 (스킵 불가)
            return false;
        }

        for (String activeProfile : activeProfiles) {
            for (String allowedProfile : AUTH_SKIP_ALLOWED_PROFILES) {
                if (activeProfile.equalsIgnoreCase(allowedProfile)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.debug("gRPC call intercepted: {}", methodName);

        // Public 메서드는 인증 스킵
        if (isPublicMethod(methodName)) {
            return next.startCall(call, headers);
        }

        // 개발 환경에서 인증 스킵 설정 확인 (프로파일 검증 포함)
        if (skipAuth && skipAuthAllowed) {
            log.debug("Auth skipped for development (grpc.auth.skip=true, profile allowed)");
            Context context = Context.current().withValue(USER_ID_KEY, "dev-user");
            return Contexts.interceptCall(context, call, headers, next);
        } else if (skipAuth && !skipAuthAllowed) {
            log.warn("grpc.auth.skip=true is configured but IGNORED in production profile!");
        }

        // Authorization 헤더에서 토큰 추출
        String authHeader = headers.get(AUTHORIZATION_KEY);

        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("Missing authorization header for method: {}", methodName);
            call.close(Status.UNAUTHENTICATED.withDescription("Missing authorization header"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        try {
            // Bearer 토큰 추출
            String token = extractBearerToken(authHeader);

            // JWT 토큰 검증
            if (!jwtService.isValidToken(token)) {
                throw new IllegalArgumentException("Invalid token");
            }

            String userId = jwtService.validateTokenAndGetUserId(token);

            // Context에 사용자 ID 저장
            Context context = Context.current().withValue(USER_ID_KEY, userId);

            return Contexts.interceptCall(context, call, headers, next);

        } catch (Exception e) {
            log.error("Authentication failed for method: {}", methodName, e);
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid token: " + e.getMessage()), headers);
            return new ServerCall.Listener<ReqT>() {};
        }
    }

    private boolean isPublicMethod(String methodName) {
        for (String publicMethod : PUBLIC_METHODS) {
            if (methodName.contains(publicMethod)) {
                return true;
            }
        }
        return false;
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    /**
     * 현재 Context에서 인증된 사용자 ID 가져오기
     */
    public static String getCurrentUserId() {
        return USER_ID_KEY.get();
    }
}
