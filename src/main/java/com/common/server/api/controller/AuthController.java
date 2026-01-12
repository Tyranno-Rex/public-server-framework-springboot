package com.common.server.api.controller;

import com.common.server.dto.common.ErrorResponse;
import com.common.server.core.service.interfaces.AuthService;
import com.common.server.dto.auth.LoginRequestDto;
import com.common.server.dto.auth.LoginResponseDto;
import com.common.server.dto.auth.RefreshTokenRequestDto;
import com.common.server.dto.auth.RefreshTokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러 (프레임워크 기본 제공)
 *
 * 로그인, 토큰 갱신, 로그아웃 등의 인증 관련 API를 제공합니다.
 * 실제 인증 로직은 AuthService 구현체에서 처리합니다.
 */
@Tag(name = "인증 API", description = "JWT 토큰 관리, 사용자 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     *
     * POST /api/auth/login
     *
     * @param requestDto 로그인 요청
     * @return LoginResponseDto (JWT 토큰)
     */
    @Operation(
            summary = "로그인",
            description = "로그인하고 JWT Access Token 및 Refresh Token을 발급받습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("로그인 요청 수신");
        LoginResponseDto response = authService.login(requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Access Token 갱신
     *
     * POST /api/auth/refresh
     *
     * @param requestDto Refresh Token 요청
     * @return RefreshTokenResponseDto (새로운 Access Token)
     */
    @Operation(
            summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (Refresh Token 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 만료된 Refresh Token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto requestDto
    ) {
        log.info("Access Token 갱신 요청 수신");
        RefreshTokenResponseDto response = authService.refreshAccessToken(requestDto.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     *
     * POST /api/auth/logout
     *
     * @param authentication Spring Security 인증 정보
     * @return 성공 메시지
     */
    @Operation(
            summary = "로그아웃",
            description = "현재 사용자의 Refresh Token을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        log.info("로그아웃 요청: userId={}", userId);

        authService.logout(userId);

        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * 헬스체크
     *
     * GET /api/auth/health
     *
     * @return 상태 메시지
     */
    @Operation(
            summary = "인증 서비스 헬스체크",
            description = "인증 서비스가 정상 작동하는지 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "서비스 정상"
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is healthy");
    }
}
