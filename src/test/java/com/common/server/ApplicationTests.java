package com.common.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Application Context 로드 테스트
 *
 * H2 인메모리 DB를 사용하여 Spring Context가 정상적으로 로드되는지 확인합니다.
 * (src/test/resources/application.properties 사용)
 */
@SpringBootTest
@DisplayName("Application Context 테스트")
class ApplicationTests {

    @Test
    @DisplayName("Spring Context 로드 성공")
    void contextLoads() {
        // Spring Context가 정상적으로 로드되면 테스트 통과
    }
}
