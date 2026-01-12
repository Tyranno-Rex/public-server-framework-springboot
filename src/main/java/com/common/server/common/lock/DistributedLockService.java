package com.common.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 분산 락 서비스
 *
 * Redis를 사용하여 분산 환경에서 락을 구현합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private static final String LOCK_PREFIX = "lock:";
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 락을 획득하고 작업 실행
     *
     * @param key 락 키
     * @param waitTime 락 대기 시간
     * @param leaseTime 락 유지 시간
     * @param timeUnit 시간 단위
     * @param supplier 실행할 작업
     * @return 작업 결과
     */
    public <T> T executeWithLock(String key, long waitTime, long leaseTime,
                                  TimeUnit timeUnit, Supplier<T> supplier) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            locked = tryLock(lockKey, lockValue, waitTime, leaseTime, timeUnit);
            if (!locked) {
                throw new LockAcquisitionException("Failed to acquire lock for key: " + key);
            }
            log.debug("Lock acquired: {}", key);
            return supplier.get();
        } finally {
            if (locked) {
                unlock(lockKey, lockValue);
                log.debug("Lock released: {}", key);
            }
        }
    }

    /**
     * 락을 획득하고 작업 실행 (Runnable)
     */
    public void executeWithLock(String key, long waitTime, long leaseTime,
                                 TimeUnit timeUnit, Runnable runnable) {
        executeWithLock(key, waitTime, leaseTime, timeUnit, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 락 획득 시도
     */
    public boolean tryLock(String lockKey, String lockValue, long waitTime,
                           long leaseTime, TimeUnit timeUnit) {
        long waitTimeMillis = timeUnit.toMillis(waitTime);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < waitTimeMillis) {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, leaseTime, timeUnit);

            if (Boolean.TRUE.equals(success)) {
                return true;
            }

            try {
                Thread.sleep(50); // 50ms 간격으로 재시도
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    /**
     * 락 해제
     */
    public void unlock(String lockKey, String lockValue) {
        Object currentValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 락 강제 해제 (관리용)
     */
    public void forceUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
        log.warn("Lock forcefully released: {}", key);
    }

    /**
     * 락 상태 확인
     */
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }
}
