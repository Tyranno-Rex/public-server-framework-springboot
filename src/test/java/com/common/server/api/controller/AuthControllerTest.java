package com.common.server.api.controller;

import com.common.server.core.service.interfaces.AuthService;
import com.common.server.dto.auth.LoginRequestDto;
import com.common.server.dto.auth.LoginResponseDto;
import com.common.server.dto.auth.RefreshTokenRequestDto;
import com.common.server.dto.auth.RefreshTokenResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController API 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController API 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private LoginRequestDto loginRequest;
    private LoginResponseDto loginResponse;

    @BeforeEach
    void setUp() {
        // 로그인 요청 DTO
        loginRequest = LoginRequestDto.builder()
                .token("test-token")
                .provider("test")
                .build();

        // 로그인 응답 DTO
        loginResponse = LoginResponseDto.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .expiresIn(900L)
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공")
    void login_Success() throws Exception {
        // given
        when(authService.login(any(LoginRequestDto.class))).thenReturn(loginResponse);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"));

        verify(authService, times(1)).login(any(LoginRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - 잘못된 요청 (null token)")
    void login_InvalidRequest_NullToken() throws Exception {
        // given
        LoginRequestDto invalidRequest = LoginRequestDto.builder()
                .token(null)
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Access Token 갱신 성공")
    void refreshToken_Success() throws Exception {
        // given
        RefreshTokenRequestDto refreshRequest = new RefreshTokenRequestDto("test-refresh-token");
        RefreshTokenResponseDto refreshResponse = RefreshTokenResponseDto.builder()
                .accessToken("new-access-token")
                .build();

        when(authService.refreshAccessToken(eq("test-refresh-token"))).thenReturn(refreshResponse);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));

        verify(authService, times(1)).refreshAccessToken(eq("test-refresh-token"));
    }

    @Test
    @DisplayName("GET /api/auth/health - 헬스체크 성공")
    void health_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Auth Service is healthy"));

        verifyNoInteractions(authService);
    }
}
