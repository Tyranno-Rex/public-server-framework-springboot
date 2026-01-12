package com.common.server.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * 페이지네이션 응답 DTO
 *
 * Spring Data의 Page를 클라이언트 친화적인 형식으로 변환합니다.
 *
 * @param <T> 응답 데이터 타입
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Getter
@Builder
@Schema(description = "페이지네이션 응답")
public class PageResponse<T> {

    @Schema(description = "데이터 목록")
    private final List<T> content;

    @Schema(description = "현재 페이지 번호 (0부터 시작)")
    private final int page;

    @Schema(description = "페이지 크기")
    private final int size;

    @Schema(description = "전체 요소 수")
    private final long totalElements;

    @Schema(description = "전체 페이지 수")
    private final int totalPages;

    @Schema(description = "첫 페이지 여부")
    private final boolean first;

    @Schema(description = "마지막 페이지 여부")
    private final boolean last;

    @Schema(description = "비어있는지 여부")
    private final boolean empty;

    @Schema(description = "다음 페이지 존재 여부")
    private final boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부")
    private final boolean hasPrevious;

    /**
     * Spring Data Page에서 변환
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
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

    /**
     * Spring Data Page에서 변환 (매핑 함수 적용)
     */
    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
                .map(mapper)
                .toList();

        return PageResponse.<R>builder()
                .content(content)
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

    /**
     * List에서 수동으로 PageResponse 생성
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(content.isEmpty())
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}
