package com.common.server.api.health;

import com.common.server.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 체크 API
 *
 * 서비스의 각 구성 요소 상태를 확인합니다.
 * - 전체 상태
 * - 데이터베이스 연결
 * - Redis 연결
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Tag(name = "Health", description = "헬스 체크 API")
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 전체 헬스 체크
     */
    @Operation(summary = "전체 헬스 체크", description = "모든 구성 요소의 상태를 확인합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", checkDatabase());
        health.put("redis", checkRedis());

        boolean allUp = "UP".equals(health.get("database").toString().contains("UP") ? "UP" : "DOWN")
                && "UP".equals(health.get("redis").toString().contains("UP") ? "UP" : "DOWN");

        if (!allUp) {
            health.put("status", "DEGRADED");
        }

        return ResponseEntity.ok(ApiResponse.success(health));
    }

    /**
     * 간단한 상태 확인 (로드밸런서용)
     */
    @Operation(summary = "간단한 상태 확인", description = "서버 동작 여부만 확인합니다.")
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }

    /**
     * 데이터베이스 상태 확인
     */
    @Operation(summary = "데이터베이스 상태 확인")
    @GetMapping("/db")
    public ResponseEntity<ApiResponse<Map<String, String>>> databaseHealth() {
        return ResponseEntity.ok(ApiResponse.success(checkDatabase()));
    }

    /**
     * Redis 상태 확인
     */
    @Operation(summary = "Redis 상태 확인")
    @GetMapping("/redis")
    public ResponseEntity<ApiResponse<Map<String, String>>> redisHealth() {
        return ResponseEntity.ok(ApiResponse.success(checkRedis()));
    }

    private Map<String, String> checkDatabase() {
        Map<String, String> result = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                result.put("status", "UP");
                result.put("database", connection.getMetaData().getDatabaseProductName());
                result.put("version", connection.getMetaData().getDatabaseProductVersion());
            } else {
                result.put("status", "DOWN");
                result.put("error", "Connection validation failed");
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, String> checkRedis() {
        Map<String, String> result = new HashMap<>();
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equals(pong)) {
                result.put("status", "UP");
            } else {
                result.put("status", "DOWN");
                result.put("error", "Unexpected response: " + pong);
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return result;
    }
}
