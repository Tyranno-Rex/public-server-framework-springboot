package com.common.server.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * RefreshToken Entity
 *
 * JWT Refresh Token을 저장하여 토큰 갱신 및 보안을 관리합니다.
 *
 * 비즈니스 규칙:
 * - 사용자당 여러 개의 Refresh Token 보유 가능 (다중 기기 로그인)
 * - 만료된 토큰은 배치 작업으로 정리
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_token", columnList = "token"),
    @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /**
     * 토큰 소유 사용자 ID
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * Refresh Token 값
     */
    @Column(name = "token", length = 512, nullable = false)
    private String token;

    /**
     * 토큰 만료 시각
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰이 만료되었는지 확인
     *
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
