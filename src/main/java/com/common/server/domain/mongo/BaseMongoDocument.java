package com.common.server.domain.mongo;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

/**
 * MongoDB Document 기본 클래스
 *
 * 모든 MongoDB Document가 상속받는 공통 필드를 정의합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Getter
public abstract class BaseMongoDocument {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
