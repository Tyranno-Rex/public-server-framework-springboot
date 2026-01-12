package com.common.server.common.feature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature Flag 서비스
 *
 * Redis를 사용하여 Feature Flag를 관리합니다.
 * Redis 연결 실패 시 로컬 캐시를 사용합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private static final String FEATURE_FLAG_PREFIX = "feature:flag:";
    private static final String FEATURE_FLAGS_SET = "feature:flags";

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 장애 시 폴백용 로컬 캐시
    private final Map<String, Boolean> localCache = new ConcurrentHashMap<>();

    /**
     * Feature Flag 활성화 여부 확인
     */
    public boolean isEnabled(String featureName) {
        try {
            String key = FEATURE_FLAG_PREFIX + featureName;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            }
        } catch (Exception e) {
            log.warn("Failed to get feature flag from Redis: {}", featureName, e);
            // 로컬 캐시 폴백
            return localCache.getOrDefault(featureName, false);
        }
        return false;
    }

    /**
     * Feature Flag 활성화 여부 확인 (기본값 지정)
     */
    public boolean isEnabled(String featureName, boolean defaultValue) {
        try {
            String key = FEATURE_FLAG_PREFIX + featureName;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            }
        } catch (Exception e) {
            log.warn("Failed to get feature flag from Redis: {}", featureName, e);
            return localCache.getOrDefault(featureName, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Feature Flag 설정
     */
    public void setFlag(String featureName, boolean enabled) {
        try {
            String key = FEATURE_FLAG_PREFIX + featureName;
            redisTemplate.opsForValue().set(key, String.valueOf(enabled));
            redisTemplate.opsForSet().add(FEATURE_FLAGS_SET, featureName);
            localCache.put(featureName, enabled);
            log.info("Feature flag '{}' set to: {}", featureName, enabled);
        } catch (Exception e) {
            log.error("Failed to set feature flag: {}", featureName, e);
            localCache.put(featureName, enabled);
        }
    }

    /**
     * Feature Flag 삭제
     */
    public void removeFlag(String featureName) {
        try {
            String key = FEATURE_FLAG_PREFIX + featureName;
            redisTemplate.delete(key);
            redisTemplate.opsForSet().remove(FEATURE_FLAGS_SET, featureName);
            localCache.remove(featureName);
            log.info("Feature flag '{}' removed", featureName);
        } catch (Exception e) {
            log.error("Failed to remove feature flag: {}", featureName, e);
            localCache.remove(featureName);
        }
    }

    /**
     * 모든 Feature Flag 조회
     */
    public Map<String, Boolean> getAllFlags() {
        Map<String, Boolean> flags = new HashMap<>();
        try {
            Set<Object> featureNames = redisTemplate.opsForSet().members(FEATURE_FLAGS_SET);
            if (featureNames != null) {
                for (Object name : featureNames) {
                    String featureName = name.toString();
                    flags.put(featureName, isEnabled(featureName));
                }
            }
        } catch (Exception e) {
            log.error("Failed to get all feature flags", e);
            return new HashMap<>(localCache);
        }
        return flags;
    }

    /**
     * Feature Flag가 활성화된 경우에만 작업 실행
     */
    public void executeIfEnabled(String featureName, Runnable action) {
        if (isEnabled(featureName)) {
            action.run();
        }
    }
}
