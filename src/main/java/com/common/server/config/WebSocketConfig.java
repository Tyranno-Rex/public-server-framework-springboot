package com.common.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 설정
 *
 * STOMP 프로토콜을 사용한 WebSocket 메시지 브로커 설정입니다.
 *
 * 활성화: application.properties에서 websocket.enabled=true
 *
 * 클라이언트 연결: ws://localhost:8080/ws
 * 구독: /topic/*, /queue/*
 * 전송: /app/*
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 prefix
        // /topic: 1:N 브로드캐스트
        // /queue: 1:1 개인 메시지
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지를 보낼 때 사용하는 prefix
        config.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지를 보낼 때 사용하는 prefix
        config.setUserDestinationPrefix("/user");

        log.info("WebSocket Message Broker configured");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 폴백 지원

        // SockJS 없는 순수 WebSocket 엔드포인트
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");

        log.info("WebSocket STOMP endpoints registered: /ws, /ws-native");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인바운드 메시지에 인증 인터셉터 추가
        registration.interceptors(webSocketAuthInterceptor);
    }
}
