package com.common.server.domain.auth.repository;

import com.common.server.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken Repository
 *
 * @author 정은성
 * @since 2025-01-08
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /**
     * 토큰으로 RefreshToken 조회
     *
     * @param token Refresh Token 값
     * @return RefreshToken
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 모든 RefreshToken 조회
     *
     * @param userId 사용자 ID
     * @return RefreshToken 목록
     */
    List<RefreshToken> findAllByUserId(String userId);

    /**
     * 사용자의 모든 RefreshToken 삭제 (로그아웃)
     *
     * @param userId 사용자 ID
     */
    void deleteAllByUserId(String userId);

    /**
     * 만료된 RefreshToken 삭제 (배치 작업용)
     *
     * @param now 현재 시각
     */
    void deleteAllByExpiresAtBefore(LocalDateTime now);
}
