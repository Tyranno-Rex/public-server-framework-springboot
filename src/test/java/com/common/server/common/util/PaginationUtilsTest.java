package com.common.server.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaginationUtils 단위 테스트")
class PaginationUtilsTest {

    @Test
    @DisplayName("toPageInfo - 페이지 정보를 Map으로 변환")
    void toPageInfo_convertsPageToMap() {
        // given
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);

        // when
        Map<String, Object> pageInfo = PaginationUtils.toPageInfo(page);

        // then
        assertThat(pageInfo.get("page")).isEqualTo(0);
        assertThat(pageInfo.get("size")).isEqualTo(10);
        assertThat(pageInfo.get("totalElements")).isEqualTo(25L);
        assertThat(pageInfo.get("totalPages")).isEqualTo(3);
        assertThat(pageInfo.get("first")).isEqualTo(true);
        assertThat(pageInfo.get("last")).isEqualTo(false);
        assertThat(pageInfo.get("hasNext")).isEqualTo(true);
        assertThat(pageInfo.get("hasPrevious")).isEqualTo(false);
    }

    @Test
    @DisplayName("toPageInfo - 마지막 페이지")
    void toPageInfo_lastPage() {
        // given
        List<String> content = List.of("item1");
        Page<String> page = new PageImpl<>(content, PageRequest.of(2, 10), 25);

        // when
        Map<String, Object> pageInfo = PaginationUtils.toPageInfo(page);

        // then
        assertThat(pageInfo.get("page")).isEqualTo(2);
        assertThat(pageInfo.get("first")).isEqualTo(false);
        assertThat(pageInfo.get("last")).isEqualTo(true);
        assertThat(pageInfo.get("hasNext")).isEqualTo(false);
        assertThat(pageInfo.get("hasPrevious")).isEqualTo(true);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "-1, 0",
            "5, 5",
            "100, 100"
    })
    @DisplayName("normalizePageNumber - 페이지 번호 정규화")
    void normalizePageNumber(int input, int expected) {
        // when
        int result = PaginationUtils.normalizePageNumber(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 20, 20",
            "-5, 20, 20",
            "10, 20, 10",
            "50, 20, 20",
            "100, 50, 50"
    })
    @DisplayName("normalizePageSize - 페이지 크기 정규화")
    void normalizePageSize(int input, int max, int expected) {
        // when
        int result = PaginationUtils.normalizePageSize(input, max);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("calculateOffset - 오프셋 계산")
    void calculateOffset() {
        // when & then
        assertThat(PaginationUtils.calculateOffset(0, 10)).isEqualTo(0);
        assertThat(PaginationUtils.calculateOffset(1, 10)).isEqualTo(10);
        assertThat(PaginationUtils.calculateOffset(5, 20)).isEqualTo(100);
    }

    @Test
    @DisplayName("calculateTotalPages - 총 페이지 수 계산")
    void calculateTotalPages() {
        // when & then
        assertThat(PaginationUtils.calculateTotalPages(0, 10)).isEqualTo(0);
        assertThat(PaginationUtils.calculateTotalPages(10, 10)).isEqualTo(1);
        assertThat(PaginationUtils.calculateTotalPages(25, 10)).isEqualTo(3);
        assertThat(PaginationUtils.calculateTotalPages(100, 20)).isEqualTo(5);
    }
}
