package com.ticketing.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.access-token-expiration}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role",role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public Long getMemberId(String token) {
        Claims claims = parse(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getRole(String token) {
        Claims claims = parse(token);
        return claims.get("role", String.class);
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

