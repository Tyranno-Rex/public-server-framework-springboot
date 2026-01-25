package com.common.server.domain.mongo.topic;

import com.common.server.domain.mongo.BaseMongoDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주제 추천 횟수 관리 Document
 *
 * 사용자별로 주제 추천 가능 횟수를 관리합니다.
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Document(collection = "topic_suggestion_quotas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicSuggestionQuota extends BaseMongoDocument {

    /**
     * 사용자 ID
     */
    @Indexed(unique = true)
    private String userId;

    /**
     * 주간 기본 횟수 (무료)
     */
    @Builder.Default
    private Integer weeklyFreeQuota = 1;

    /**
     * 이번 주에 사용한 무료 횟수
     */
    @Builder.Default
    private Integer weeklyUsedQuota = 0;

    /**
     * 주간 리셋 날짜 (매주 월요일)
     */
    private LocalDate weekResetDate;

    /**
     * 추가 구매한 횟수 (누적)
     */
    @Builder.Default
    private Integer purchasedQuota = 0;

    /**
     * 월간 무제한 구독 여부
     */
    @Builder.Default
    private Boolean hasUnlimitedSubscription = false;

    /**
     * 월간 무제한 구독 만료일
     */
    private LocalDateTime subscriptionExpiresAt;

    /**
     * 남은 추천 가능 횟수 계산
     *
     * @return 남은 횟수
     */
    public int getRemainingQuota() {
        // 무제한 구독 중이고 만료 전이면 무한대
        if (hasUnlimitedSubscription && subscriptionExpiresAt != null
                && LocalDateTime.now().isBefore(subscriptionExpiresAt)) {
            return Integer.MAX_VALUE;
        }

        // 주간 무료 횟수 + 구매한 횟수 - 사용한 횟수
        int weeklyRemaining = Math.max(0, weeklyFreeQuota - weeklyUsedQuota);
        return weeklyRemaining + purchasedQuota;
    }

    /**
     * 주제 추천 사용
     *
     * @return 사용 성공 여부
     */
    public boolean useSuggestion() {
        if (getRemainingQuota() <= 0) {
            return false;
        }

        // 무제한 구독 중이면 횟수 차감 안 함
        if (hasUnlimitedSubscription && subscriptionExpiresAt != null
                && LocalDateTime.now().isBefore(subscriptionExpiresAt)) {
            return true;
        }

        // 주간 무료 횟수 먼저 사용
        if (weeklyUsedQuota < weeklyFreeQuota) {
            weeklyUsedQuota++;
            return true;
        }

        // 구매한 횟수 사용
        if (purchasedQuota > 0) {
            purchasedQuota--;
            return true;
        }

        return false;
    }

    /**
     * 주간 무료 횟수 리셋
     */
    public void resetWeeklyQuota() {
        this.weeklyUsedQuota = 0;
        this.weekResetDate = getNextMonday();
    }

    /**
     * 추가 횟수 구매
     *
     * @param count 추가할 횟수
     */
    public void addPurchasedQuota(int count) {
        this.purchasedQuota += count;
    }

    /**
     * 월간 무제한 구독 시작
     *
     * @param days 구독 일수
     */
    public void startUnlimitedSubscription(int days) {
        this.hasUnlimitedSubscription = true;
        this.subscriptionExpiresAt = LocalDateTime.now().plusDays(days);
    }

    /**
     * 다음 월요일 날짜 계산
     */
    private LocalDate getNextMonday() {
        LocalDate today = LocalDate.now();
        int daysUntilMonday = (8 - today.getDayOfWeek().getValue()) % 7;
        if (daysUntilMonday == 0) daysUntilMonday = 7; // 오늘이 월요일이면 다음 주
        return today.plusDays(daysUntilMonday);
    }
}
