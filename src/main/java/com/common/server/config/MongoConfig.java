package com.common.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB 설정
 *
 * - MongoDB Auditing 활성화 (createdAt, updatedAt 자동 관리)
 * - MongoDB Repository 스캔
 *
 * 활성화: application.properties에서 spring.data.mongodb.enabled=true
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.common.server.repository.mongo")
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = false)
public class MongoConfig {
}
