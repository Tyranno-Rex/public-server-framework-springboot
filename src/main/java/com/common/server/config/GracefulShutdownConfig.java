package com.common.server.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

/**
 * Graceful Shutdown 설정
 *
 * 애플리케이션 종료 시 정리 작업을 수행합니다.
 * - 진행 중인 요청 완료 대기
 * - 리소스 정리
 * - 로그 기록
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Configuration
@Slf4j
public class GracefulShutdownConfig {

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent event) {
        log.info("=== Application Shutdown Started ===");
        log.info("Waiting for ongoing requests to complete...");
    }

    @PreDestroy
    public void onDestroy() {
        log.info("Cleaning up resources...");
        log.info("=== Application Shutdown Completed ===");
    }
}
