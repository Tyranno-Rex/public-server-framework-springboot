package com.common.server.common.feature;

import com.common.server.exception.FeatureDisabledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Feature Flag AOP Aspect
 *
 * @FeatureFlag 어노테이션이 붙은 메서드를 Feature Flag 상태에 따라 실행합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagAspect {

    private final FeatureFlagService featureFlagService;

    @Around("@annotation(featureFlag)")
    public Object checkFeatureFlag(ProceedingJoinPoint joinPoint, FeatureFlag featureFlag) throws Throwable {
        String featureName = featureFlag.value();
        boolean isEnabled = featureFlagService.isEnabled(featureName);

        if (!isEnabled) {
            log.debug("Feature '{}' is disabled. Method: {}", featureName, joinPoint.getSignature().getName());

            if (featureFlag.throwIfDisabled()) {
                throw new FeatureDisabledException(featureName);
            }

            return null;
        }

        return joinPoint.proceed();
    }
}
