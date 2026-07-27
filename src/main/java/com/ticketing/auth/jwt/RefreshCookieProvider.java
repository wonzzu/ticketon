package com.ticketing.auth.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieProvider {

    public static final String COOKIE_NAME = "refreshToken";

    private final String path;

    public RefreshCookieProvider(@Value("${app.refresh-cookie-path:/auth}") String path) {
        this.path = path;
    }

    public ResponseCookie create(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie expired() {
        return create("", Duration.ZERO);
    }
}
