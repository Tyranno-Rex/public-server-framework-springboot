package com.common.server.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse 단위 테스트")
class ApiResponseTest {

    @Test
    @DisplayName("success() - 데이터 없이 성공 응답 생성")
    void success_withoutData() {
        // when
        ApiResponse<Void> response = ApiResponse.success();

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("success(data) - 데이터와 함께 성공 응답 생성")
    void success_withData() {
        // given
        String data = "test data";

        // when
        ApiResponse<String> response = ApiResponse.success(data);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("success(data, message) - 메시지와 함께 성공 응답 생성")
    void success_withDataAndMessage() {
        // given
        Integer data = 42;
        String message = "Operation completed";

        // when
        ApiResponse<Integer> response = ApiResponse.success(data, message);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("error(code, message) - 에러 응답 생성")
    void error_basic() {
        // given
        String code = "ERR001";
        String message = "Something went wrong";

        // when
        ApiResponse<Object> response = ApiResponse.error(code, message);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo(code);
        assertThat(response.getError().getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("error(code, message, details) - 상세 정보와 함께 에러 응답 생성")
    void error_withDetails() {
        // given
        String code = "VALIDATION_ERROR";
        String message = "Validation failed";
        String details = "Field 'email' is invalid";

        // when
        ApiResponse<Object> response = ApiResponse.error(code, message, details);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getCode()).isEqualTo(code);
        assertThat(response.getError().getMessage()).isEqualTo(message);
        assertThat(response.getError().getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("타임스탬프가 현재 시간 근처")
    void timestamp_isRecent() {
        // given
        long before = System.currentTimeMillis();

        // when
        ApiResponse<Void> response = ApiResponse.success();

        // then
        long after = System.currentTimeMillis();
        long responseTime = response.getTimestamp().toInstant(
                java.time.ZoneOffset.systemDefault().getRules().getOffset(response.getTimestamp())
        ).toEpochMilli();

        assertThat(responseTime).isBetween(before, after);
    }
}
