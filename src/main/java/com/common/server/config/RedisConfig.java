package com.common.server.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 *
 * Spring Cache Abstraction을 사용한 Redis 캐싱 설정
 * 캐시별 TTL은 application.properties에서 설정 가능
 * 테스트 환경에서는 비활성화됨 (spring.cache.type=none)
 *
 * @author DDIP Team
 * @since 2026-01-03
 */
@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.common.server.domain.*.repository.redis")
public class RedisConfig {

    @Value("${cache.ttl.user:3600000}")
    private long userCacheTtl;

    @Value("${cache.ttl.post:300000}")
    private long postCacheTtl;

    @Value("${cache.ttl.ddip:60000}")
    private long ddipCacheTtl;

    @Value("${cache.ttl.discovery:180000}")
    private long discoveryCacheTtl;

    @Value("${cache.ttl.comment:300000}")
    private long commentCacheTtl;

    /**
     * Redis Template 설정
     * - Key는 String으로 직렬화
     * - Value는 JSON으로 직렬화
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache Manager 설정
     * - 캐시별로 다른 TTL 설정
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration (10분 TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(objectMapper())
            ))
            .disableCachingNullValues();

        // 캐시별 커스텀 TTL 설정 (application.properties에서 로드)
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMillis(userCacheTtl)));
        cacheConfigurations.put("post", defaultConfig.entryTtl(Duration.ofMillis(postCacheTtl)));
        cacheConfigurations.put("ddip", defaultConfig.entryTtl(Duration.ofMillis(ddipCacheTtl)));
        cacheConfigurations.put("discovery", defaultConfig.entryTtl(Duration.ofMillis(discoveryCacheTtl)));
        cacheConfigurations.put("comment", defaultConfig.entryTtl(Duration.ofMillis(commentCacheTtl)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }

    /**
     * ObjectMapper for JSON serialization
     * - JavaTimeModule 추가 (LocalDateTime 등 지원)
     * - Type information 포함 (역직렬화 시 타입 정보 유지)
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Polymorphic type handling with validator
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)
            .build();

        mapper.activateDefaultTyping(
            validator,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }
}
