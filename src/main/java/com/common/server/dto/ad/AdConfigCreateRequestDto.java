package com.common.server.dto.ad;

import com.common.server.domain.mongo.ad.AdConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 광고 설정 생성 요청 DTO
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdConfigCreateRequestDto {

    @NotNull(message = "광고 타입은 필수입니다")
    private AdConfig.AdType type;

    @NotBlank(message = "광고 이미지 URL은 필수입니다")
    private String imageUrl;

    private String clickUrl;

    private Boolean isActive = true;

    private Integer priority = 0;

    private String startDate;

    private String endDate;

    private String title;

    private String description;
}
