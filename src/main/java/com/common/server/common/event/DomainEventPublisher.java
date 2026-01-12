package com.common.server.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 도메인 이벤트 발행기
 *
 * Spring의 ApplicationEventPublisher를 래핑하여 도메인 이벤트를 발행합니다.
 *
 * 사용 예시:
 * <pre>
 * {@literal @}Service
 * {@literal @}RequiredArgsConstructor
 * public class OrderService {
 *     private final DomainEventPublisher eventPublisher;
 *
 *     public void createOrder(Order order) {
 *         // 주문 생성 로직...
 *         eventPublisher.publish(new OrderCreatedEvent(order.getId()));
 *     }
 * }
 * </pre>
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 도메인 이벤트 발행
     *
     * @param event 발행할 이벤트
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} (id: {})", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 일반 이벤트 발행
     *
     * @param event 발행할 이벤트 객체
     */
    public void publish(Object event) {
        log.debug("Publishing event: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
