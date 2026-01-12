package com.common.server.common.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * MDC Task Decorator
 *
 * 비동기 작업(@Async)에서도 MDC 컨텍스트를 유지하도록 합니다.
 * AsyncConfig의 ThreadPoolTaskExecutor에 적용하여 사용합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 현재 스레드의 MDC 값을 복사
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 새 스레드에 MDC 값 설정
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // MDC 클리어
                MDC.clear();
            }
        };
    }
}
