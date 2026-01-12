package com.common.server.common.event;

import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.*;

/**
 * 비동기 이벤트 리스너 어노테이션
 *
 * 트랜잭션 완료 후 비동기로 이벤트를 처리합니다.
 *
 * 사용 예시:
 * <pre>
 * {@literal @}Component
 * public class OrderEventHandler {
 *
 *     {@literal @}AsyncEventListener
 *     public void handleOrderCreated(OrderCreatedEvent event) {
 *         // 비동기로 이메일 발송, 알림 전송 등
 *     }
 * }
 * </pre>
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Async("notificationExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public @interface AsyncEventListener {

    /**
     * 이벤트 클래스 (기본: 메서드 파라미터 타입)
     */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "classes")
    Class<?>[] value() default {};

    /**
     * 조건 (SpEL 표현식)
     */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "condition")
    String condition() default "";

    /**
     * 트랜잭션 단계
     */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "phase")
    TransactionPhase phase() default TransactionPhase.AFTER_COMMIT;

    /**
     * 트랜잭션이 없을 때 실행 여부
     */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "fallbackExecution")
    boolean fallbackExecution() default false;
}
