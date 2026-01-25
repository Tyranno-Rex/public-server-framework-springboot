package com.common.server.domain.mongo.topic;

import com.common.server.domain.mongo.BaseMongoDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 주제 추천 횟수 구매 이력 Document
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Document(collection = "topic_suggestion_purchases")
@CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicSuggestionPurchase extends BaseMongoDocument {

    /**
     * 사용자 ID
     */
    @Indexed
    private String userId;

    /**
     * 상품 ID
     */
    private String productId;

    /**
     * 상품 코드
     */
    private String productCode;

    /**
     * 상품명
     */
    private String productName;

    /**
     * 구매 가격
     */
    private Integer price;

    /**
     * 추가된 횟수 (ONE_TIME, PACKAGE의 경우)
     */
    private Integer quotaAdded;

    /**
     * 구독 일수 (MONTHLY_UNLIMITED의 경우)
     */
    private Integer subscriptionDays;

    /**
     * 결제 방법 (향후 확장)
     */
    private PaymentMethod paymentMethod;

    /**
     * 결제 상태
     */
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 결제 트랜잭션 ID (실제 결제 연동 시 사용)
     */
    private String transactionId;

    public enum PaymentMethod {
        /**
         * 테스트 (개발용)
         */
        TEST,

        /**
         * 신용카드
         */
        CREDIT_CARD,

        /**
         * 간편결제
         */
        EASY_PAY,

        /**
         * 앱스토어 인앱결제
         */
        APP_STORE,

        /**
         * 구글 플레이 인앱결제
         */
        GOOGLE_PLAY
    }

    public enum PaymentStatus {
        /**
         * 대기 중
         */
        PENDING,

        /**
         * 완료
         */
        COMPLETED,

        /**
         * 실패
         */
        FAILED,

        /**
         * 환불
         */
        REFUNDED
    }

    /**
     * 결제 완료 처리
     */
    public void complete(String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
    }

    /**
     * 결제 실패 처리
     */
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}
