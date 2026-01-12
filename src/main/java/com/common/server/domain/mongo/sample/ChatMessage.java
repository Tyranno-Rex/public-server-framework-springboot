package com.common.server.domain.mongo.sample;

import com.common.server.domain.mongo.BaseMongoDocument;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 Document (샘플)
 *
 * MongoDB에 저장되는 채팅 메시지 예시입니다.
 * 실제 사용 시 수정하거나 삭제하세요.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Document(collection = "chat_messages")
@CompoundIndex(name = "room_time_idx", def = "{'roomId': 1, 'createdAt': -1}")
@Getter
@Builder
public class ChatMessage extends BaseMongoDocument {

    @Indexed
    private String roomId;

    private String senderId;

    private String senderName;

    private String content;

    private MessageType type;

    private LocalDateTime readAt;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }
}
