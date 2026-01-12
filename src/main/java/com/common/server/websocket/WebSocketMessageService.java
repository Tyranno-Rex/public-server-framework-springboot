package com.common.server.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 메시지 전송 서비스
 *
 * 서버에서 클라이언트로 메시지를 전송하는 서비스입니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 특정 토픽으로 메시지 브로드캐스트
     *
     * @param topic 토픽 경로 (예: /topic/chat/room1)
     * @param message 전송할 메시지
     */
    public void sendToTopic(String topic, Object message) {
        log.debug("Sending message to topic: {}", topic);
        messagingTemplate.convertAndSend(topic, message);
    }

    /**
     * 특정 사용자에게 메시지 전송
     *
     * @param userId 사용자 ID
     * @param destination 목적지 (예: /queue/notifications)
     * @param message 전송할 메시지
     */
    public void sendToUser(String userId, String destination, Object message) {
        log.debug("Sending message to user: {} at {}", userId, destination);
        messagingTemplate.convertAndSendToUser(userId, destination, message);
    }

    /**
     * 채팅방에 메시지 전송
     *
     * @param roomId 채팅방 ID
     * @param message 전송할 메시지
     */
    public void sendToChatRoom(String roomId, Object message) {
        sendToTopic("/topic/chat/" + roomId, message);
    }

    /**
     * 사용자에게 알림 전송
     *
     * @param userId 사용자 ID
     * @param notification 알림 내용
     */
    public void sendNotification(String userId, Object notification) {
        sendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * 전체 공지 브로드캐스트
     *
     * @param announcement 공지 내용
     */
    public void broadcastAnnouncement(Object announcement) {
        sendToTopic("/topic/announcements", announcement);
    }
}
