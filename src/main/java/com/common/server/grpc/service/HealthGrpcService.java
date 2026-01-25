package com.common.server.grpc.service;

import com.common.server.grpc.health.HealthCheckResponse;
import com.common.server.grpc.health.HealthServiceGrpc;
import com.common.server.grpc.health.VersionResponse;
import com.common.server.grpc.common.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

/**
 * Health Check gRPC Service
 *
 * Example usage in client:
 * - Check health: healthService.check()
 * - Get version: healthService.version()
 */
@Slf4j
@GrpcService
public class HealthGrpcService extends HealthServiceGrpc.HealthServiceImplBase {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.build-time:unknown}")
    private String buildTime;

    @Value("${app.git-commit:unknown}")
    private String gitCommit;

    @Override
    public void check(Empty request, StreamObserver<HealthCheckResponse> responseObserver) {
        log.debug("Health check requested");

        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setHealthy(true)
                .setStatus("UP")
                .setTimestamp(Instant.now().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void version(Empty request, StreamObserver<VersionResponse> responseObserver) {
        log.debug("Version info requested");

        VersionResponse response = VersionResponse.newBuilder()
                .setVersion(appVersion)
                .setBuildTime(buildTime)
                .setGitCommit(gitCommit)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
