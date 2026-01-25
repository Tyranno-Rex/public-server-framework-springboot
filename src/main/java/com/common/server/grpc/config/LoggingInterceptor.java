package com.common.server.grpc.config;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * gRPC Logging Interceptor
 * Logs all incoming gRPC requests and responses
 */
@Slf4j
public class LoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("gRPC call started: {}", methodName);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                    @Override
                    public void sendMessage(RespT message) {
                        log.debug("gRPC response: {}", methodName);
                        super.sendMessage(message);
                    }

                    @Override
                    public void close(Status status, Metadata trailers) {
                        if (status.isOk()) {
                            log.info("gRPC call completed: {}", methodName);
                        } else {
                            log.error("gRPC call failed: {} - {}", methodName, status);
                        }
                        super.close(status, trailers);
                    }
                }, headers)) {
        };
    }
}
