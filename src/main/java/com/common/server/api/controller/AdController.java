package com.common.server.api.controller;

import com.common.server.core.service.AdService;
import com.common.server.domain.mongo.ad.AdConfig;
import com.common.server.dto.ad.AdConfigCreateRequestDto;
import com.common.server.dto.ad.AdConfigDto;
import com.common.server.dto.ad.AdEventRequestDto;
import com.common.server.dto.ad.AdStatisticsDto;
import com.common.server.dto.common.ApiResponse;
import com.common.server.dto.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 광고 관리 컨트롤러
 *
 * 광고 설정 CRUD 및 조회 API를 제공합니다.
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Tag(name = "광고 API", description = "광고 설정 관리 및 조회 API")
@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Slf4j
public class AdController {

    private final AdService adService;

    /**
     * 광고 설정 생성 (관리자)
     *
     * POST /api/ads
     *
     * @param requestDto 광고 설정 생성 요청
     * @return 생성된 광고 설정
     */
    @Operation(
            summary = "광고 설정 생성",
            description = "새로운 광고 설정을 생성합니다. (관리자 전용)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "광고 설정 생성 성공",
                    content = @Content(schema = @Schema(implementation = AdConfigDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AdConfigDto>> createAdConfig(
            @Valid @RequestBody AdConfigCreateRequestDto requestDto
    ) {
        log.info("광고 설정 생성 요청: type={}", requestDto.getType());
        AdConfigDto adConfig = adService.createAdConfig(requestDto);
        return ResponseEntity.ok(ApiResponse.success(adConfig));
    }

    /**
     * 활성화된 광고 목록 조회
     *
     * GET /api/ads/active
     *
     * @return 활성화된 광고 목록
     */
    @Operation(
            summary = "활성화된 광고 목록 조회",
            description = "활성화된 모든 광고를 우선순위 순으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AdConfigDto>>> getActiveAds() {
        log.info("활성화된 광고 목록 조회");
        List<AdConfigDto> ads = adService.getActiveAdConfigs();
        return ResponseEntity.ok(ApiResponse.success(ads));
    }

    /**
     * 타입별 활성화된 광고 목록 조회
     *
     * GET /api/ads/active/{type}
     *
     * @param type 광고 타입 (INTERSTITIAL, BANNER, NATIVE)
     * @return 활성화된 광고 목록
     */
    @Operation(
            summary = "타입별 활성화된 광고 목록 조회",
            description = "특정 타입의 활성화된 광고를 우선순위 순으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/active/{type}")
    public ResponseEntity<ApiResponse<List<AdConfigDto>>> getActiveAdsByType(
            @PathVariable AdConfig.AdType type
    ) {
        log.info("타입별 광고 목록 조회: type={}", type);
        List<AdConfigDto> ads = adService.getActiveAdConfigsByType(type);
        return ResponseEntity.ok(ApiResponse.success(ads));
    }

    /**
     * 전면 광고 조회 (앱 진입 시 사용)
     *
     * GET /api/ads/interstitial
     *
     * @return 전면 광고 (없으면 null)
     */
    @Operation(
            summary = "전면 광고 조회",
            description = "앱 진입 시 표시할 전면 광고를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/interstitial")
    public ResponseEntity<ApiResponse<AdConfigDto>> getInterstitialAd() {
        log.info("전면 광고 조회 요청");
        AdConfigDto ad = adService.getRandomInterstitialAd();
        return ResponseEntity.ok(ApiResponse.success(ad));
    }

    /**
     * 광고 설정 상세 조회
     *
     * GET /api/ads/{id}
     *
     * @param id 광고 설정 ID
     * @return 광고 설정
     */
    @Operation(
            summary = "광고 설정 상세 조회",
            description = "특정 광고 설정의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AdConfigDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "광고 설정을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdConfigDto>> getAdConfig(@PathVariable String id) {
        log.info("광고 설정 상세 조회: id={}", id);
        AdConfigDto adConfig = adService.getAdConfig(id);
        return ResponseEntity.ok(ApiResponse.success(adConfig));
    }

    /**
     * 광고 설정 삭제 (관리자)
     *
     * DELETE /api/ads/{id}
     *
     * @param id 광고 설정 ID
     * @return 성공 메시지
     */
    @Operation(
            summary = "광고 설정 삭제",
            description = "특정 광고 설정을 삭제합니다. (관리자 전용)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "광고 설정을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAdConfig(@PathVariable String id) {
        log.info("광고 설정 삭제 요청: id={}", id);
        adService.deleteAdConfig(id);
        return ResponseEntity.ok(ApiResponse.success("광고 설정이 삭제되었습니다."));
    }

    /**
     * 광고 노출 이벤트 기록
     *
     * POST /api/ads/impression
     *
     * @param requestDto 광고 이벤트 요청
     * @return 성공 메시지
     */
    @Operation(
            summary = "광고 노출 이벤트 기록",
            description = "광고가 노출되었을 때 호출하여 통계를 기록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "기록 성공"
            )
    })
    @PostMapping("/impression")
    public ResponseEntity<ApiResponse<String>> recordImpression(
            @Valid @RequestBody AdEventRequestDto requestDto
    ) {
        log.info("광고 노출 기록 요청: adId={}", requestDto.getAdId());
        adService.recordImpression(requestDto.getAdId(), requestDto.getUserId());
        return ResponseEntity.ok(ApiResponse.success("광고 노출이 기록되었습니다."));
    }

    /**
     * 광고 클릭 이벤트 기록
     *
     * POST /api/ads/click
     *
     * @param requestDto 광고 이벤트 요청
     * @return 성공 메시지
     */
    @Operation(
            summary = "광고 클릭 이벤트 기록",
            description = "광고가 클릭되었을 때 호출하여 통계를 기록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "기록 성공"
            )
    })
    @PostMapping("/click")
    public ResponseEntity<ApiResponse<String>> recordClick(
            @Valid @RequestBody AdEventRequestDto requestDto
    ) {
        log.info("광고 클릭 기록 요청: adId={}", requestDto.getAdId());
        adService.recordClick(requestDto.getAdId(), requestDto.getUserId());
        return ResponseEntity.ok(ApiResponse.success("광고 클릭이 기록되었습니다."));
    }

    /**
     * 광고 통계 조회 (특정 광고, 특정 날짜)
     *
     * GET /api/ads/{id}/statistics
     *
     * @param id 광고 ID
     * @param date 날짜 (선택, 기본값: 오늘)
     * @return 통계
     */
    @Operation(
            summary = "광고 통계 조회",
            description = "특정 광고의 특정 날짜 통계를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<AdStatisticsDto>> getStatistics(
            @PathVariable String id,
            @RequestParam(required = false) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        log.info("광고 통계 조회 요청: adId={}, date={}", id, targetDate);
        AdStatisticsDto statistics = adService.getStatistics(id, targetDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 광고 통계 조회 (특정 광고, 기간)
     *
     * GET /api/ads/{id}/statistics/period
     *
     * @param id 광고 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 목록
     */
    @Operation(
            summary = "광고 기간별 통계 조회",
            description = "특정 광고의 기간별 통계를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/{id}/statistics/period")
    public ResponseEntity<ApiResponse<List<AdStatisticsDto>>> getStatisticsByPeriod(
            @PathVariable String id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        log.info("광고 기간별 통계 조회 요청: adId={}, start={}, end={}", id, startDate, endDate);
        List<AdStatisticsDto> statistics = adService.getStatisticsByPeriod(id, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 광고 통계 조회 (특정 광고, 최근 30일)
     *
     * GET /api/ads/{id}/statistics/recent
     *
     * @param id 광고 ID
     * @return 통계 목록
     */
    @Operation(
            summary = "광고 최근 통계 조회",
            description = "특정 광고의 최근 30일 통계를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/{id}/statistics/recent")
    public ResponseEntity<ApiResponse<List<AdStatisticsDto>>> getRecentStatistics(
            @PathVariable String id
    ) {
        log.info("광고 최근 통계 조회 요청: adId={}", id);
        List<AdStatisticsDto> statistics = adService.getRecentStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 전체 광고 통계 조회 (특정 날짜)
     *
     * GET /api/ads/statistics/daily
     *
     * @param date 날짜 (선택, 기본값: 오늘)
     * @return 통계 목록
     */
    @Operation(
            summary = "일별 전체 광고 통계 조회",
            description = "특정 날짜의 전체 광고 통계를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/statistics/daily")
    public ResponseEntity<ApiResponse<List<AdStatisticsDto>>> getAllStatisticsByDate(
            @RequestParam(required = false) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        log.info("일별 전체 광고 통계 조회 요청: date={}", targetDate);
        List<AdStatisticsDto> statistics = adService.getAllStatisticsByDate(targetDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
