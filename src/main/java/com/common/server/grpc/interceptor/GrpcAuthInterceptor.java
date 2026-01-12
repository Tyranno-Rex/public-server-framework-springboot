package com.common.server.grpc.interceptor;

import com.common.server.core.service.interfaces.JwtService;
import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * gRPC 인증 인터셉터
 *
 * JWT 토큰을 검증하고 사용자 정보를 Context에 저장
 * 환경 설정(grpc.auth.skip)에 따라 인증 스킵 가능
 *
 * @author DDIP Team
 * @since 2025-01-13
 */
@GrpcGlobalServerInterceptor
public class GrpcAuthInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcAuthInterceptor.class);

    // Metadata 키 정의
    public static final Metadata.Key<String> AUTHORIZATION_KEY =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    // Context 키 - 인증된 사용자 ID 저장
    public static final Context.Key<String> USER_ID_KEY = Context.key("userId");

    // 인증이 필요없는 메서드 목록
    private static final String[] PUBLIC_METHODS = {
        // 필요시 public 메서드 추가
    };

    private final JwtService jwtService;

    @Value("${grpc.auth.skip:false}")
    private boolean skipAuth;

    public GrpcAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
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

        // 개발 환경에서 인증 스킵 설정 확인
        if (skipAuth) {
            log.debug("Auth skipped for development (grpc.auth.skip=true)");
            Context context = Context.current().withValue(USER_ID_KEY, "dev-user");
            return Contexts.interceptCall(context, call, headers, next);
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
