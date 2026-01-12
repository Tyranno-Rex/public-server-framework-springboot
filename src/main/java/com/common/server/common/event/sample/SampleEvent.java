package com.common.server.common.event.sample;

import com.common.server.common.event.DomainEvent;
import lombok.Getter;

/**
 * 샘플 도메인 이벤트
 *
 * 이벤트 구조 예시입니다. 실제 사용 시 삭제하거나 수정하세요.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Getter
public class SampleEvent extends DomainEvent {

    private final String resourceId;
    private final String action;
    private final String userId;

    public SampleEvent(String resourceId, String action, String userId) {
        super();
        this.resourceId = resourceId;
        this.action = action;
        this.userId = userId;
    }
}
