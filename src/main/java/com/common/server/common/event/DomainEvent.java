package com.common.server.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트 기본 클래스
 *
 * 모든 도메인 이벤트의 부모 클래스입니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getEventType() {
        return eventType;
    }
}
