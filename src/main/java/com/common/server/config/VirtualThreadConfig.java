package com.common.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Java 21 Virtual Threads 설정
 *
 * Virtual Threads를 사용하여 높은 동시성을 효율적으로 처리합니다.
 * - Tomcat에서 Virtual Threads 사용
 * - Async 작업에서 Virtual Threads 사용
 *
 * 활성화: application.properties에서 spring.threads.virtual.enabled=true
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Configuration
@EnableAsync
@Slf4j
@ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true", matchIfMissing = false)
public class VirtualThreadConfig {

    /**
     * Tomcat에서 Virtual Threads 사용
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        log.info("=== Virtual Threads ENABLED for Tomcat ===");
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    /**
     * Async 작업에서 Virtual Threads 사용
     */
    @Bean(name = "virtualThreadExecutor")
    public AsyncTaskExecutor virtualThreadExecutor() {
        log.info("=== Virtual Thread Executor created ===");
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Virtual Thread Factory (직접 사용 가능)
     */
    @Bean
    public Thread.Builder.OfVirtual virtualThreadBuilder() {
        return Thread.ofVirtual().name("virtual-", 0);
    }
}
