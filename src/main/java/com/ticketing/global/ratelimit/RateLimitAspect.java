package com.ticketing.global.ratelimit;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static com.ticketing.global.baseresponse.BaseResponseStatus.TOO_MANY_REQUESTS;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    @Before("@annotation(rateLimit)")
    public void limit(JoinPoint joinPoint, RateLimit rateLimit) {
        String identifier = resolveIdentifier(rateLimit.key());
        String key = "ratelimit:" + joinPoint.getSignature().toShortString() + ":" + identifier;

        Long count = redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(rateLimit.windowSeconds())
        );

        if (count > rateLimit.limit()) {
            throw new BaseException(TOO_MANY_REQUESTS);
        }
    }



    private String resolveIdentifier(RateLimit.KeyType keyType) {
        return keyType == RateLimit.KeyType.MEMBER ? currentMemberId() : currentIp();
    }

    private String currentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getMemberId().toString();
        }
        return "anonymous";
    }

    private String currentIp() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
