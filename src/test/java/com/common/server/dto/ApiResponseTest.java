package com.common.server.dto;

import com.common.server.dto.common.ApiResponse;
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
        assertThat(response.getCode()).isEqualTo("SUCCESS");
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
        assertThat(response.getCode()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("success(message, data) - 메시지와 함께 성공 응답 생성")
    void success_withMessageAndData() {
        // given
        Integer data = 42;
        String message = "Operation completed";

        // when
        ApiResponse<Integer> response = ApiResponse.success(message, data);

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
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("created(data) - 생성 성공 응답")
    void created_withData() {
        // given
        String data = "new resource";

        // when
        ApiResponse<String> response = ApiResponse.created(data);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("CREATED");
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    @DisplayName("deleted() - 삭제 성공 응답")
    void deleted() {
        // when
        ApiResponse<Void> response = ApiResponse.deleted();

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("DELETED");
    }
}
