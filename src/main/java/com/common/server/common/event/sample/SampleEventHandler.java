package com.common.server.common.event.sample;

import com.common.server.common.event.AsyncEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 샘플 이벤트 핸들러
 *
 * 이벤트 핸들러 구조 예시입니다. 실제 사용 시 삭제하거나 수정하세요.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Component
@Slf4j
public class SampleEventHandler {

    /**
     * 비동기로 샘플 이벤트 처리
     */
    @AsyncEventListener
    public void handleSampleEvent(SampleEvent event) {
        log.info("[EVENT] Handling SampleEvent - id: {}, action: {}, resource: {}, user: {}",
                event.getEventId(),
                event.getAction(),
                event.getResourceId(),
                event.getUserId());

        // 실제 로직: 이메일 발송, 알림 전송, 외부 API 호출 등
    }
}
