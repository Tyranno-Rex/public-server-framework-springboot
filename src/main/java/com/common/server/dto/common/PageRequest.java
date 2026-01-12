package com.common.server.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 페이지네이션 요청 DTO
 *
 * 클라이언트로부터 페이지네이션 정보를 받아 Spring Data의 Pageable로 변환합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지네이션 요청")
public class PageRequest {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Builder.Default
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Builder.Default
    private int size = 20;

    @Schema(description = "정렬 필드", example = "createdAt")
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC")
    @Builder.Default
    private String sortDirection = "DESC";

    // 최대 페이지 크기 제한
    private static final int MAX_SIZE = 100;

    /**
     * Spring Data Pageable로 변환
     */
    public Pageable toPageable() {
        int validSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validPage = Math.max(0, page);

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                sortBy
        );

        return org.springframework.data.domain.PageRequest.of(validPage, validSize, sort);
    }

    /**
     * 기본 정렬(최신순) Pageable로 변환
     */
    public Pageable toPageableDescById() {
        int validSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validPage = Math.max(0, page);

        return org.springframework.data.domain.PageRequest.of(
                validPage,
                validSize,
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    /**
     * 커스텀 정렬 Pageable로 변환
     */
    public Pageable toPageable(Sort sort) {
        int validSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validPage = Math.max(0, page);

        return org.springframework.data.domain.PageRequest.of(validPage, validSize, sort);
    }
}
