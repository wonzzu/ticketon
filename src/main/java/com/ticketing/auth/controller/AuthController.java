package com.ticketing.auth.controller;


import com.ticketing.auth.dto.request.LoginRequestDto;
import com.ticketing.auth.dto.response.TokenResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/auth";
    private static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofDays(14);

    private final AuthService authService;

    @RateLimit(key = RateLimit.KeyType.IP, limit = 5, windowSeconds = 60)
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(@Validated @RequestBody LoginRequestDto dto) {
        TokenPair pair = authService.login(dto);

        ResponseCookie responseCookie = buildRefreshCookie(pair.refreshToken(), REFRESH_COOKIE_MAX_AGE);

        TokenResponse data = TokenResponse.builder().accessToken(pair.accessToken()).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(BaseResponse.success(data));

    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        String newAccessToken = authService.reissue(refreshToken);

        TokenResponse data = TokenResponse.builder().accessToken(newAccessToken).build();

        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout() {

        ResponseCookie expired = buildRefreshCookie("", Duration.ZERO);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(BaseResponse.success());
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }


}
