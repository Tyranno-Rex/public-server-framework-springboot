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
 * 광고 노출 통계 Document
 *
 * 광고 노출 및 클릭 이벤트를 기록합니다.
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Document(collection = "ad_impressions")
@CompoundIndex(name = "ad_date_idx", def = "{'adId': 1, 'date': -1}")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdImpression extends BaseMongoDocument {

    /**
     * 광고 설정 ID
     */
    @Indexed
    private String adId;

    /**
     * 노출 날짜 (집계용)
     */
    @Indexed
    private LocalDate date;

    /**
     * 노출 횟수
     */
    @Builder.Default
    private Long impressionCount = 0L;

    /**
     * 클릭 횟수
     */
    @Builder.Default
    private Long clickCount = 0L;

    /**
     * 사용자 ID (선택사항)
     */
    private String userId;

    /**
     * 디바이스 정보 (선택사항)
     */
    private String deviceInfo;

    /**
     * 노출 증가
     */
    public void incrementImpression() {
        this.impressionCount++;
    }

    /**
     * 클릭 증가
     */
    public void incrementClick() {
        this.clickCount++;
    }

    /**
     * CTR (Click Through Rate) 계산
     *
     * @return CTR (%)
     */
    public double getCtr() {
        if (impressionCount == 0) {
            return 0.0;
        }
        return (double) clickCount / impressionCount * 100;
    }
}
