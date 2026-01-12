package com.common.server.grpc.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC 예외 처리 인터셉터
 *
 * 서비스에서 발생하는 예외를 적절한 gRPC Status로 변환
 *
 * @author DDIP Team
 * @since 2025-01-13
 */
@GrpcGlobalServerInterceptor
public class GrpcExceptionInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcExceptionInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> listener = next.startCall(
            new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                @Override
                public void close(Status status, Metadata trailers) {
                    if (status.getCode() == Status.Code.UNKNOWN && status.getCause() != null) {
                        // 예외를 적절한 Status로 변환
                        Status newStatus = mapExceptionToStatus(status.getCause());
                        super.close(newStatus, trailers);
                    } else {
                        super.close(status, trailers);
                    }
                }
            },
            headers
        );

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    log.error("gRPC exception caught: {}", e.getMessage(), e);
                    Status status = mapExceptionToStatus(e);
                    call.close(status, new Metadata());
                }
            }

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (Exception e) {
                    log.error("gRPC onMessage exception: {}", e.getMessage(), e);
                    Status status = mapExceptionToStatus(e);
                    call.close(status, new Metadata());
                }
            }
        };
    }

    /**
     * Java 예외를 gRPC Status로 매핑
     */
    private Status mapExceptionToStatus(Throwable throwable) {
        String message = throwable.getMessage() != null ? throwable.getMessage() : "Unknown error";

        // 예외 타입에 따라 적절한 Status 반환
        if (throwable instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(message).withCause(throwable);
        }

        if (throwable instanceof IllegalStateException) {
            return Status.FAILED_PRECONDITION.withDescription(message).withCause(throwable);
        }

        if (throwable instanceof SecurityException) {
            return Status.PERMISSION_DENIED.withDescription(message).withCause(throwable);
        }

        if (throwable instanceof NullPointerException) {
            return Status.INTERNAL.withDescription("Null pointer exception: " + message).withCause(throwable);
        }

        if (throwable.getClass().getSimpleName().contains("NotFound")) {
            return Status.NOT_FOUND.withDescription(message).withCause(throwable);
        }

        if (throwable.getClass().getSimpleName().contains("Unauthorized") ||
            throwable.getClass().getSimpleName().contains("Authentication")) {
            return Status.UNAUTHENTICATED.withDescription(message).withCause(throwable);
        }

        // 기본값: INTERNAL 에러
        log.error("Unmapped exception type: {}", throwable.getClass().getName());
        return Status.INTERNAL.withDescription(message).withCause(throwable);
    }
}
