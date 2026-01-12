package com.common.server.api.v1.sample;

import com.common.server.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 샘플 API 컨트롤러 (v1)
 *
 * API 버저닝 예시를 위한 샘플 컨트롤러입니다.
 * 실제 사용 시 이 파일을 삭제하거나 수정하세요.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Tag(name = "Sample", description = "샘플 API (v1)")
@RestController
@RequestMapping("/sample")
public class SampleController {

    @Operation(summary = "샘플 조회", description = "API 버저닝 테스트용 샘플 엔드포인트")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getSample() {
        Map<String, String> data = Map.of(
                "version", "v1",
                "message", "This is API v1 sample endpoint"
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
