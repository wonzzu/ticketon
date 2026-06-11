package com.ticketing.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpirationMs;

    private String key(Long memberId) {
        return "refresh:" + memberId;
    }

    public void save(Long memberId, String token) {
        redisTemplate.opsForValue().set(key(memberId), token, Duration.ofMillis(refreshExpirationMs));
    }

    public boolean isValid(Long memberId, String token) {
        String saved = redisTemplate.opsForValue().get(key(memberId));
        return saved != null && saved.equals(token);
    }

    public void delete(Long memberId) {
        redisTemplate.delete(key(memberId));
    }
}
