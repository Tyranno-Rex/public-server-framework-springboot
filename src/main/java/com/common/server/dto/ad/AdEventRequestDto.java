package com.common.server.dto.ad;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 광고 이벤트 기록 요청 DTO
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdEventRequestDto {

    @NotBlank(message = "광고 ID는 필수입니다")
    private String adId;

    /**
     * 사용자 ID (선택사항, 로그인한 경우)
     */
    private String userId;

    /**
     * 디바이스 정보 (선택사항)
     */
    private String deviceInfo;
}
