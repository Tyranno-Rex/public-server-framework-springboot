package com.common.server.dto.ad;

import com.common.server.domain.mongo.ad.AdStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 광고 통계 DTO
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdStatisticsDto {

    private String id;
    private String adId;
    private LocalDate date;
    private Long totalImpressions;
    private Long totalClicks;
    private Long uniqueUsers;
    private Double ctr;

    /**
     * Entity to DTO
     */
    public static AdStatisticsDto from(AdStatistics statistics) {
        return AdStatisticsDto.builder()
                .id(statistics.getId())
                .adId(statistics.getAdId())
                .date(statistics.getDate())
                .totalImpressions(statistics.getTotalImpressions())
                .totalClicks(statistics.getTotalClicks())
                .uniqueUsers(statistics.getUniqueUsers())
                .ctr(statistics.getCtr())
                .build();
    }
}
