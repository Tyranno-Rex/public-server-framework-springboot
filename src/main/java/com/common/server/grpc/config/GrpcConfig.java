package com.common.server.grpc.config;

import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC Configuration
 *
 * Configure global interceptors, error handling, etc.
 *
 * application.yml 설정 예시:
 * grpc:
 *   server:
 *     port: 9090
 *     max-inbound-message-size: 10MB
 */
@Slf4j
@Configuration
public class GrpcConfig {

    /**
     * Global gRPC interceptor for logging
     */
    @GrpcGlobalServerInterceptor
    public ServerInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    /**
     * TODO: Add authentication interceptor
     * @GrpcGlobalServerInterceptor
     * public ServerInterceptor authInterceptor() {
     *     return new AuthInterceptor();
     * }
     */
}
