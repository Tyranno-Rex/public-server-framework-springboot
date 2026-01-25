package com.common.server.domain.mongo.ad;

import com.common.server.domain.mongo.BaseMongoDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * 광고 일별 통계 Document
 *
 * 일별로 집계된 광고 통계를 저장합니다.
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Document(collection = "ad_statistics")
@CompoundIndex(name = "ad_date_unique_idx", def = "{'adId': 1, 'date': 1}", unique = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdStatistics extends BaseMongoDocument {

    /**
     * 광고 설정 ID
     */
    @Indexed
    private String adId;

    /**
     * 통계 날짜
     */
    @Indexed
    private LocalDate date;

    /**
     * 총 노출 횟수
     */
    @Builder.Default
    private Long totalImpressions = 0L;

    /**
     * 총 클릭 횟수
     */
    @Builder.Default
    private Long totalClicks = 0L;

    /**
     * 고유 사용자 수
     */
    @Builder.Default
    private Long uniqueUsers = 0L;

    /**
     * 노출 증가
     */
    public void incrementImpression() {
        this.totalImpressions++;
    }

    /**
     * 클릭 증가
     */
    public void incrementClick() {
        this.totalClicks++;
    }

    /**
     * CTR (Click Through Rate) 계산
     *
     * @return CTR (%)
     */
    public double getCtr() {
        if (totalImpressions == 0) {
            return 0.0;
        }
        return (double) totalClicks / totalImpressions * 100;
    }
}
