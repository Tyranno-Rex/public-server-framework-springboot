package com.common.server.exception;

/**
 * Feature Flag가 비활성화된 경우 발생하는 예외
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
public class FeatureDisabledException extends RuntimeException {

    private final String featureName;

    public FeatureDisabledException(String featureName) {
        super("Feature '" + featureName + "' is currently disabled");
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
