package com.common.server.websocket.sample;

import com.common.server.websocket.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 채팅 WebSocket 컨트롤러 (샘플)
 *
 * WebSocket STOMP 메시지를 처리하는 예시 컨트롤러입니다.
 * 실제 사용 시 수정하거나 삭제하세요.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
public class ChatController {

    private final WebSocketMessageService messageService;

    /**
     * 채팅 메시지 전송
     *
     * 클라이언트: stompClient.send("/app/chat/room1", {}, JSON.stringify(message))
     * 구독: stompClient.subscribe("/topic/chat/room1", callback)
     */
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessageDto message,
                            Principal principal,
                            SimpMessageHeaderAccessor headerAccessor) {

        String senderId = principal != null ? principal.getName() : "anonymous";

        log.debug("Chat message received - room: {}, sender: {}", roomId, senderId);

        // 메시지에 서버 정보 추가
        Map<String, Object> response = Map.of(
                "roomId", roomId,
                "senderId", senderId,
                "content", message.content(),
                "type", message.type(),
                "timestamp", LocalDateTime.now().toString()
        );

        // 해당 채팅방 구독자들에게 메시지 전송
        messageService.sendToChatRoom(roomId, response);
    }

    /**
     * 채팅방 입장 알림
     */
    @MessageMapping("/chat/{roomId}/join")
    public void joinRoom(@DestinationVariable String roomId,
                         Principal principal) {

        String userId = principal != null ? principal.getName() : "anonymous";

        log.info("User {} joined room {}", userId, roomId);

        Map<String, Object> joinMessage = Map.of(
                "type", "JOIN",
                "userId", userId,
                "roomId", roomId,
                "timestamp", LocalDateTime.now().toString()
        );

        messageService.sendToChatRoom(roomId, joinMessage);
    }

    /**
     * 채팅방 퇴장 알림
     */
    @MessageMapping("/chat/{roomId}/leave")
    public void leaveRoom(@DestinationVariable String roomId,
                          Principal principal) {

        String userId = principal != null ? principal.getName() : "anonymous";

        log.info("User {} left room {}", userId, roomId);

        Map<String, Object> leaveMessage = Map.of(
                "type", "LEAVE",
                "userId", userId,
                "roomId", roomId,
                "timestamp", LocalDateTime.now().toString()
        );

        messageService.sendToChatRoom(roomId, leaveMessage);
    }

    /**
     * 채팅 메시지 DTO
     */
    public record ChatMessageDto(
            String content,
            String type
    ) {}
}
