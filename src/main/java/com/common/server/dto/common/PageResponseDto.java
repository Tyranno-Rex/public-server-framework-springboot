package com.common.server.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지네이션 응답 DTO
 *
 * Spring Data의 Page 객체를 직접 JSON으로 직렬화하면 경고가 발생하므로
 * 안정적인 JSON 구조를 위해 커스텀 DTO로 변환합니다.
 *
 * @param <T> 응답 데이터 타입
 * @author DDIP Team
 * @since 2025-01-08
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {

    /** 현재 페이지의 데이터 목록 */
    private List<T> content;

    /** 현재 페이지 번호 (0부터 시작) */
    private int page;

    /** 페이지 크기 */
    private int size;

    /** 전체 요소 개수 */
    private long totalElements;

    /** 전체 페이지 개수 */
    private int totalPages;

    /** 첫 페이지 여부 */
    private boolean first;

    /** 마지막 페이지 여부 */
    private boolean last;

    /** 빈 페이지 여부 */
    private boolean empty;

    /** 다음 페이지 존재 여부 */
    private boolean hasNext;

    /** 이전 페이지 존재 여부 */
    private boolean hasPrevious;

    /**
     * Spring Data의 Page 객체를 PageResponseDto로 변환
     *
     * @param page Spring Data Page 객체
     * @param <T> 데이터 타입
     * @return PageResponseDto
     */
    public static <T> PageResponseDto<T> of(Page<T> page) {
        return PageResponseDto.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
