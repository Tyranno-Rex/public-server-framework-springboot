package com.common.server.repository.mongo.ad;

import com.common.server.domain.mongo.ad.AdStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 광고 통계 Repository
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Repository
public interface AdStatisticsRepository extends MongoRepository<AdStatistics, String> {

    /**
     * 특정 광고의 특정 날짜 통계 조회
     *
     * @param adId 광고 ID
     * @param date 날짜
     * @return 통계
     */
    Optional<AdStatistics> findByAdIdAndDate(String adId, LocalDate date);

    /**
     * 특정 광고의 기간별 통계 조회
     *
     * @param adId 광고 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 목록
     */
    List<AdStatistics> findByAdIdAndDateBetweenOrderByDateDesc(
            String adId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 특정 날짜의 전체 광고 통계 조회
     *
     * @param date 날짜
     * @return 통계 목록
     */
    List<AdStatistics> findByDateOrderByTotalImpressionsDesc(LocalDate date);

    /**
     * 특정 광고의 최근 N일 통계 조회
     *
     * @param adId 광고 ID
     * @return 통계 목록
     */
    List<AdStatistics> findTop30ByAdIdOrderByDateDesc(String adId);
}
