package com.common.server.repository.mongo.sample;

import com.common.server.domain.mongo.sample.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 Repository (샘플)
 *
 * MongoDB에 저장된 채팅 메시지를 조회합니다.
 * 실제 사용 시 수정하거나 삭제하세요.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 특정 채팅방의 메시지 조회 (페이징)
     */
    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);

    /**
     * 특정 채팅방의 최근 메시지 조회
     */
    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(String roomId);

    /**
     * 특정 시간 이후의 메시지 조회
     */
    List<ChatMessage> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(String roomId, LocalDateTime after);

    /**
     * 특정 채팅방의 메시지 개수 조회
     */
    long countByRoomId(String roomId);

    /**
     * 특정 채팅방의 메시지 삭제
     */
    void deleteByRoomId(String roomId);
}
