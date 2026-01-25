package com.common.server.repository.mongo.ad;

import com.common.server.domain.mongo.ad.AdConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 광고 설정 Repository
 *
 * @author SOCIA
 * @since 2025-01-25
 */
@Repository
public interface AdConfigRepository extends MongoRepository<AdConfig, String> {

    /**
     * 광고 타입으로 활성화된 광고 목록 조회 (우선순위 오름차순)
     *
     * @param type 광고 타입
     * @param isActive 활성화 여부
     * @return 광고 목록
     */
    List<AdConfig> findByTypeAndIsActiveTrueOrderByPriorityAsc(AdConfig.AdType type);

    /**
     * 활성화된 전체 광고 목록 조회 (우선순위 오름차순)
     *
     * @return 광고 목록
     */
    List<AdConfig> findByIsActiveTrueOrderByPriorityAsc();
}
