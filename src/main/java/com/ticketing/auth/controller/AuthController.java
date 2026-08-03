package com.ticketing.auth.controller;


import com.ticketing.auth.dto.request.LoginRequestDto;
import com.ticketing.auth.dto.response.TokenResponse;
import com.ticketing.auth.jwt.RefreshCookieProvider;
import com.ticketing.auth.service.AuthService;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.global.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.ticketing.auth.service.AuthService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofDays(14);

    private final AuthService authService;
    private final RefreshCookieProvider refreshCookieProvider;

    @RateLimit(key = RateLimit.KeyType.IP, limit = 20, windowSeconds = 60)
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(@Validated @RequestBody LoginRequestDto dto) {
        TokenPair pair = authService.login(dto);

        ResponseCookie responseCookie = refreshCookieProvider.create(pair.refreshToken(), REFRESH_COOKIE_MAX_AGE);

        TokenResponse data = TokenResponse.builder().accessToken(pair.accessToken()).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(BaseResponse.success(data));

    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refresh(
            @CookieValue(value = RefreshCookieProvider.COOKIE_NAME, required = false) String refreshToken) {
        String newAccessToken = authService.reissue(refreshToken);

        TokenResponse data = TokenResponse.builder().accessToken(newAccessToken).build();

        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @CookieValue(value = RefreshCookieProvider.COOKIE_NAME, required = false) String refreshToken) {

        authService.logout(refreshToken);

        ResponseCookie expired = refreshCookieProvider.expired();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(BaseResponse.success());
    }
}
