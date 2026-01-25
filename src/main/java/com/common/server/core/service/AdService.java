package com.common.server.core.service;

import com.common.server.domain.mongo.ad.AdConfig;
import com.common.server.domain.mongo.ad.AdStatistics;
import com.common.server.dto.ad.AdConfigCreateRequestDto;
import com.common.server.dto.ad.AdConfigDto;
import com.common.server.dto.ad.AdStatisticsDto;
import com.common.server.repository.mongo.ad.AdConfigRepository;
import com.common.server.repository.mongo.ad.AdStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 광고 설정 Service
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdService {

    private final AdConfigRepository adConfigRepository;
    private final AdStatisticsRepository adStatisticsRepository;

    /**
     * 광고 설정 생성
     *
     * @param requestDto 광고 설정 생성 요청
     * @return 생성된 광고 설정
     */
    @Transactional
    public AdConfigDto createAdConfig(AdConfigCreateRequestDto requestDto) {
        AdConfig adConfig = AdConfig.builder()
                .type(requestDto.getType())
                .imageUrl(requestDto.getImageUrl())
                .clickUrl(requestDto.getClickUrl())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .priority(requestDto.getPriority() != null ? requestDto.getPriority() : 0)
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .build();

        AdConfig saved = adConfigRepository.save(adConfig);
        log.info("광고 설정 생성 완료: {}", saved.getId());

        return AdConfigDto.from(saved);
    }

    /**
     * 활성화된 광고 목록 조회 (우선순위 순)
     *
     * @return 광고 목록
     */
    @Transactional(readOnly = true)
    public List<AdConfigDto> getActiveAdConfigs() {
        List<AdConfig> adConfigs = adConfigRepository.findByIsActiveTrueOrderByPriorityAsc();
        return adConfigs.stream()
                .map(AdConfigDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 타입별 활성화된 광고 목록 조회
     *
     * @param type 광고 타입
     * @return 광고 목록
     */
    @Transactional(readOnly = true)
    public List<AdConfigDto> getActiveAdConfigsByType(AdConfig.AdType type) {
        List<AdConfig> adConfigs = adConfigRepository.findByTypeAndIsActiveTrueOrderByPriorityAsc(type);
        return adConfigs.stream()
                .map(AdConfigDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 광고 설정 상세 조회
     *
     * @param id 광고 설정 ID
     * @return 광고 설정
     */
    @Transactional(readOnly = true)
    public AdConfigDto getAdConfig(String id) {
        AdConfig adConfig = adConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고 설정을 찾을 수 없습니다: " + id));
        return AdConfigDto.from(adConfig);
    }

    /**
     * 광고 설정 삭제
     *
     * @param id 광고 설정 ID
     */
    @Transactional
    public void deleteAdConfig(String id) {
        adConfigRepository.deleteById(id);
        log.info("광고 설정 삭제 완료: {}", id);
    }

    /**
     * 전면 광고 랜덤 조회 (앱 진입 시 사용)
     *
     * @return 광고 설정 (없으면 null)
     */
    @Transactional(readOnly = true)
    public AdConfigDto getRandomInterstitialAd() {
        List<AdConfig> interstitialAds = adConfigRepository
                .findByTypeAndIsActiveTrueOrderByPriorityAsc(AdConfig.AdType.INTERSTITIAL);

        if (interstitialAds.isEmpty()) {
            return null;
        }

        // 우선순위가 가장 높은 광고 반환 (첫 번째)
        // TODO: 랜덤 또는 로테이션 로직 추가 가능
        return AdConfigDto.from(interstitialAds.get(0));
    }

    /**
     * 광고 노출 이벤트 기록
     *
     * @param adId 광고 ID
     * @param userId 사용자 ID (선택)
     */
    @Transactional
    public void recordImpression(String adId, String userId) {
        LocalDate today = LocalDate.now();

        AdStatistics statistics = adStatisticsRepository
                .findByAdIdAndDate(adId, today)
                .orElse(AdStatistics.builder()
                        .adId(adId)
                        .date(today)
                        .totalImpressions(0L)
                        .totalClicks(0L)
                        .uniqueUsers(0L)
                        .build());

        statistics.incrementImpression();
        adStatisticsRepository.save(statistics);

        log.info("광고 노출 기록: adId={}, userId={}", adId, userId);
    }

    /**
     * 광고 클릭 이벤트 기록
     *
     * @param adId 광고 ID
     * @param userId 사용자 ID (선택)
     */
    @Transactional
    public void recordClick(String adId, String userId) {
        LocalDate today = LocalDate.now();

        AdStatistics statistics = adStatisticsRepository
                .findByAdIdAndDate(adId, today)
                .orElse(AdStatistics.builder()
                        .adId(adId)
                        .date(today)
                        .totalImpressions(0L)
                        .totalClicks(0L)
                        .uniqueUsers(0L)
                        .build());

        statistics.incrementClick();
        adStatisticsRepository.save(statistics);

        log.info("광고 클릭 기록: adId={}, userId={}", adId, userId);
    }

    /**
     * 광고 통계 조회 (특정 광고, 특정 날짜)
     *
     * @param adId 광고 ID
     * @param date 날짜
     * @return 통계
     */
    @Transactional(readOnly = true)
    public AdStatisticsDto getStatistics(String adId, LocalDate date) {
        AdStatistics statistics = adStatisticsRepository
                .findByAdIdAndDate(adId, date)
                .orElse(AdStatistics.builder()
                        .adId(adId)
                        .date(date)
                        .totalImpressions(0L)
                        .totalClicks(0L)
                        .uniqueUsers(0L)
                        .build());

        return AdStatisticsDto.from(statistics);
    }

    /**
     * 광고 통계 조회 (특정 광고, 기간)
     *
     * @param adId 광고 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 목록
     */
    @Transactional(readOnly = true)
    public List<AdStatisticsDto> getStatisticsByPeriod(String adId, LocalDate startDate, LocalDate endDate) {
        List<AdStatistics> statistics = adStatisticsRepository
                .findByAdIdAndDateBetweenOrderByDateDesc(adId, startDate, endDate);

        return statistics.stream()
                .map(AdStatisticsDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 광고 통계 조회 (특정 광고, 최근 30일)
     *
     * @param adId 광고 ID
     * @return 통계 목록
     */
    @Transactional(readOnly = true)
    public List<AdStatisticsDto> getRecentStatistics(String adId) {
        List<AdStatistics> statistics = adStatisticsRepository
                .findTop30ByAdIdOrderByDateDesc(adId);

        return statistics.stream()
                .map(AdStatisticsDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 전체 광고 통계 조회 (특정 날짜)
     *
     * @param date 날짜
     * @return 통계 목록
     */
    @Transactional(readOnly = true)
    public List<AdStatisticsDto> getAllStatisticsByDate(LocalDate date) {
        List<AdStatistics> statistics = adStatisticsRepository
                .findByDateOrderByTotalImpressionsDesc(date);

        return statistics.stream()
                .map(AdStatisticsDto::from)
                .collect(Collectors.toList());
    }
}
