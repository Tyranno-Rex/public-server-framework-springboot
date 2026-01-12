package com.common.server.common.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistributedLockService 단위 테스트")
class DistributedLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private DistributedLockService lockService;

    @BeforeEach
    void setUp() {
        lockService = new DistributedLockService(redisTemplate);
    }

    @Test
    @DisplayName("tryLock - 락 획득 성공")
    void tryLock_success() {
        // given
        String key = "test-lock";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:" + key), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        // when
        boolean result = lockService.tryLock(key, 10, TimeUnit.SECONDS);

        // then
        assertThat(result).isTrue();
        verify(valueOperations).setIfAbsent(eq("lock:" + key), anyString(), eq(10L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("tryLock - 락 획득 실패")
    void tryLock_failure() {
        // given
        String key = "test-lock";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:" + key), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        // when
        boolean result = lockService.tryLock(key, 10, TimeUnit.SECONDS);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("unlock - 락 해제")
    void unlock() {
        // given
        String key = "test-lock";
        when(redisTemplate.delete("lock:" + key)).thenReturn(true);

        // when
        lockService.unlock(key);

        // then
        verify(redisTemplate).delete("lock:" + key);
    }

    @Test
    @DisplayName("executeWithLock - 락 획득 후 작업 실행")
    void executeWithLock_success() {
        // given
        String key = "test-lock";
        AtomicInteger counter = new AtomicInteger(0);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:" + key), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(redisTemplate.delete("lock:" + key)).thenReturn(true);

        // when
        String result = lockService.executeWithLock(key, 5, 10, TimeUnit.SECONDS, () -> {
            counter.incrementAndGet();
            return "success";
        });

        // then
        assertThat(result).isEqualTo("success");
        assertThat(counter.get()).isEqualTo(1);
        verify(redisTemplate).delete("lock:" + key);
    }

    @Test
    @DisplayName("executeWithLock - 락 획득 실패 시 예외")
    void executeWithLock_lockFailed() {
        // given
        String key = "test-lock";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:" + key), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
                lockService.executeWithLock(key, 0, 10, TimeUnit.SECONDS, () -> "result")
        ).isInstanceOf(LockAcquisitionException.class)
                .hasMessageContaining(key);
    }

    @Test
    @DisplayName("executeWithLock - 작업 중 예외 발생 시 락 해제")
    void executeWithLock_exceptionInTask() {
        // given
        String key = "test-lock";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:" + key), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(redisTemplate.delete("lock:" + key)).thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                lockService.executeWithLock(key, 0, 10, TimeUnit.SECONDS, () -> {
                    throw new RuntimeException("Task failed");
                })
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Task failed");

        // 예외 발생해도 락은 해제되어야 함
        verify(redisTemplate).delete("lock:" + key);
    }

    @Test
    @DisplayName("isLocked - 락 상태 확인")
    void isLocked() {
        // given
        String key = "test-lock";
        when(redisTemplate.hasKey("lock:" + key)).thenReturn(true);

        // when
        boolean result = lockService.isLocked(key);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey("lock:" + key);
    }
}
