package com.common.server.dto.ad;

import com.common.server.domain.mongo.ad.AdConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 광고 설정 DTO
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdConfigDto {

    private String id;
    private String type;
    private String imageUrl;
    private String clickUrl;
    private Boolean isActive;
    private Integer priority;
    private String startDate;
    private String endDate;
    private String title;
    private String description;

    /**
     * Entity to DTO
     */
    public static AdConfigDto from(AdConfig adConfig) {
        return AdConfigDto.builder()
                .id(adConfig.getId())
                .type(adConfig.getType().name())
                .imageUrl(adConfig.getImageUrl())
                .clickUrl(adConfig.getClickUrl())
                .isActive(adConfig.getIsActive())
                .priority(adConfig.getPriority())
                .startDate(adConfig.getStartDate())
                .endDate(adConfig.getEndDate())
                .title(adConfig.getTitle())
                .description(adConfig.getDescription())
                .build();
    }
}
