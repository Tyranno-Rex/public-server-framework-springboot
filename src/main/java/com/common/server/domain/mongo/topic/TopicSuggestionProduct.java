package com.common.server.domain.mongo.topic;

import com.common.server.domain.mongo.BaseMongoDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 주제 추천 횟수 상품 Document
 *
 * 구매 가능한 주제 추천 횟수 상품을 관리합니다.
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Document(collection = "topic_suggestion_products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicSuggestionProduct extends BaseMongoDocument {

    /**
     * 상품 코드
     */
    private String productCode;

    /**
     * 상품명
     */
    private String name;

    /**
     * 상품 설명
     */
    private String description;

    /**
     * 상품 타입
     */
    private ProductType type;

    /**
     * 추가되는 추천 횟수 (ONE_TIME, PACKAGE의 경우)
     */
    private Integer quotaCount;

    /**
     * 구독 일수 (MONTHLY_UNLIMITED의 경우)
     */
    private Integer subscriptionDays;

    /**
     * 가격 (원)
     */
    private Integer price;

    /**
     * 활성화 여부
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 정렬 순서
     */
    @Builder.Default
    private Integer displayOrder = 0;

    public enum ProductType {
        /**
         * 1회 추가 (500원)
         */
        ONE_TIME,

        /**
         * 5회 패키지 (2,000원)
         */
        PACKAGE,

        /**
         * 월간 무제한 (5,000원)
         */
        MONTHLY_UNLIMITED
    }
}
