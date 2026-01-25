package com.common.server.domain.mongo.ad;

import com.common.server.domain.mongo.BaseMongoDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 광고 설정 Document
 *
 * 앱에 표시될 광고 정보를 관리합니다.
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Document(collection = "ad_configs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdConfig extends BaseMongoDocument {

    /**
     * 광고 타입
     */
    @Indexed
    private AdType type;

    /**
     * 광고 이미지 URL
     */
    private String imageUrl;

    /**
     * 광고 클릭 시 이동할 URL
     */
    private String clickUrl;

    /**
     * 광고 활성화 여부
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 광고 우선순위 (낮을수록 먼저 표시)
     */
    @Builder.Default
    private Integer priority = 0;

    /**
     * 광고 표시 시작 일시
     */
    private String startDate;

    /**
     * 광고 표시 종료 일시
     */
    private String endDate;

    /**
     * 광고 제목 (선택사항)
     */
    private String title;

    /**
     * 광고 설명 (선택사항)
     */
    private String description;

    public enum AdType {
        /**
         * 전면 광고 (앱 진입 시)
         */
        INTERSTITIAL,

        /**
         * 배너 광고 (하단 배너 등)
         */
        BANNER,

        /**
         * 네이티브 광고 (콘텐츠 내 자연스럽게 삽입)
         */
        NATIVE
    }
}
