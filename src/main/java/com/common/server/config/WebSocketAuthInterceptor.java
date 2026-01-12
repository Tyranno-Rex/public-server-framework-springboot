package com.common.server.config;

import com.common.server.core.service.interfaces.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 인증 인터셉터
 *
 * STOMP CONNECT 시 JWT 토큰을 검증합니다.
 *
 * 클라이언트에서 연결 시 Authorization 헤더에 토큰 전달:
 * stompClient.connect({'Authorization': 'Bearer xxx'}, ...)
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = getAuthHeader(accessor);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String userId = jwtService.validateTokenAndGetUserId(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("WebSocket authenticated: userId={}", userId);

                } catch (Exception e) {
                    log.warn("WebSocket authentication failed: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid token");
                }
            } else {
                log.debug("WebSocket connection without authentication");
            }
        }

        return message;
    }

    private String getAuthHeader(StompHeaderAccessor accessor) {
        // Authorization 헤더에서 토큰 가져오기
        List<String> authorization = accessor.getNativeHeader("Authorization");
        if (authorization != null && !authorization.isEmpty()) {
            return authorization.get(0);
        }

        // access_token 쿼리 파라미터에서 토큰 가져오기 (SockJS 폴백용)
        List<String> accessToken = accessor.getNativeHeader("access_token");
        if (accessToken != null && !accessToken.isEmpty()) {
            return "Bearer " + accessToken.get(0);
        }

        return null;
    }
}
