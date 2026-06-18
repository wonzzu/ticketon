# 11. Redis 총정리 & Rate Limiting

> 앞 문서들에 흩어져 있던 **Redis 용도를 한 장에 모으고**, 아직 안 다룬 Rate Limiting(요청 횟수 제한)을 설명한다.
> "이 프로젝트에서 Redis를 어디에 왜 썼나"를 면접에서 한 번에 답하기 위한 정리 문서다.

---

## 1. Redis가 하는 일 — 6가지

| 용도 | 자료구조 / 명령 | 키 | 문서 |
|---|---|---|---|
| **좌석 선점** | String + `SET NX` + TTL | `seat:hold:{sch}:{seat}` | [07](07_좌석선점_Redis.md) |
| **대기열** | Sorted Set + `INCR` | `queue:wait/active/seq:{sch}` | [08](08_대기열.md) |
| **선착순 쿠폰** | Set(`SADD`) + String(`DECR`) | `coupon:issued/stock:{id}` | [09](09_선착순쿠폰.md) |
| **리프레시 토큰** | String + TTL | `refresh:{memberId}` | [03](03_인증인가.md) |
| **Rate Limiting** | String(`INCR`) + TTL | `ratelimit:{method}:{id}` | 이 문서 |
| **분산락** | Redisson `RLock` | `queue:admit:lock` | [08 §6](08_대기열.md) |
| (캐시) | RedisCacheManager | `event`, `events` | [05 §7](05_공연_좌석.md) — **현재 비활성** |

핵심 통찰: **동시성·핫스팟·임시성**이 있는 건 전부 Redis로 뺐다. DB는 영구·정합성 데이터만 맡는다.

---

## 2. 왜 이것들을 Redis로?

세 가지 공통 이유:

1. **원자성**: Redis는 단일 스레드로 명령을 순차 처리한다. `SET NX`·`INCR`·`DECR`·`SADD`가 그 자체로 원자적이라 Race Condition을 락 없이 막는다. (좌석·쿠폰·순번)
2. **TTL**: "7분 점유", "14일 토큰", "60초 윈도우"처럼 **수명이 있는 데이터**는 만료를 Redis가 알아서 청소한다. DB로 하면 만료 배치를 따로 돌려야 한다.
3. **DB 부하 분리**: 1만 명 대기열 진입, 5000명 쿠폰 클릭이 전부 DB로 가면 죽는다. Redis(인메모리)가 핫스팟을 흡수하고, DB엔 **결제 확정 같은 진짜 쓰기만** 남긴다([ARCHITECTURE.md](ARCHITECTURE.md)의 부하 분포 참고).

> CLAUDE.md §6.7: "좌석 선점/대기열 같은 핫스팟은 DB 인덱스로 해결하지 말고 Redis로 우회." 이 원칙의 실현이 위 표 전체다.

---

## 3. 두 가지 Redis 클라이언트 — RedisTemplate vs Redisson

이 프로젝트는 Redis 접근을 두 가지로 한다.

| | `StringRedisTemplate` | `RedissonClient` |
|---|---|---|
| 용도 | 단순 명령(SET/GET/INCR/ZADD/SADD) | 분산락(`RLock`) |
| 쓰는 곳 | 좌석·쿠폰·대기열·토큰·RateLimit | 대기열 승급 락 |
| 설정 | Spring Boot 자동(`spring.data.redis.*`) | [RedissonConfig](../src/main/java/com/ticketing/config/RedissonConfig.java) 수동 빈 |

```java
// RedissonConfig — 현재 단일 서버 모드
config.useSingleServer().setAddress("redis://" + host + ":" + port);
```
- 명령 하나로 충분한 원자성(좌석·쿠폰)은 `StringRedisTemplate`.
- "여러 인스턴스 중 하나만 작업" 같은 **락**이 필요하면 Redisson([08 §6](08_대기열.md)).

---

## 4. Rate Limiting — 연타·봇 막기

같은 사람/IP가 짧은 시간에 너무 많이 부르는 걸 막는다. **AOP 어노테이션** 한 줄로 적용되게 만들었다.

### 어노테이션
[RateLimit.java](../src/main/java/com/ticketing/global/ratelimit/RateLimit.java)
```java
@Target(METHOD) @Retention(RUNTIME)
public @interface RateLimit {
    KeyType key();        // IP 기준이냐 MEMBER 기준이냐
    int limit();          // 허용 횟수
    int windowSeconds();  // 시간 창(초)
    enum KeyType { IP, MEMBER }
}
```

### 쓰는 법 — 컨트롤러에 한 줄
[AuthController](../src/main/java/com/ticketing/auth/controller/AuthController.java) / [NormalMemberController](../src/main/java/com/ticketing/member/controller/NormalMemberController.java)
```java
@RateLimit(key = RateLimit.KeyType.IP, limit = 20, windowSeconds = 60)  // IP당 60초에 20회
@PostMapping("/login")
public ... login(...) { ... }
```
로그인·회원가입·셀러가입에 적용 → 무차별 대입/스팸 가입 차단.

### 구현 — AOP + Redis 카운터
[RateLimitAspect.java](../src/main/java/com/ticketing/global/ratelimit/RateLimitAspect.java)
```java
@Before("@annotation(rateLimit)")
public void limit(JoinPoint joinPoint, RateLimit rateLimit) {
    String id = resolveIdentifier(rateLimit.key());                  // IP 또는 memberId
    String key = "ratelimit:" + joinPoint.getSignature().toShortString() + ":" + id;

    Long count = redisTemplate.opsForValue().increment(key);         // INCR (원자적, 첫 호출이면 1)
    if (count != null && count == 1L)                                 // 첫 요청일 때만
        redisTemplate.expire(key, Duration.ofSeconds(rateLimit.windowSeconds()));  // 윈도우 TTL 설정
    if (count != null && count > rateLimit.limit())
        throw new BaseException(TOO_MANY_REQUESTS);                  // 429
}
```

동작 원리(**고정 윈도우 카운터**):
1. 요청마다 `INCR` → 카운트 증가(키 없으면 자동으로 1부터).
2. **첫 요청일 때만**(`count == 1`) TTL을 건다 → 그 시점부터 `windowSeconds` 동안 같은 키가 유지.
3. 카운트가 `limit` 초과면 `TOO_MANY_REQUESTS`(429). 윈도우가 만료되면 키가 사라지고 카운트 리셋.

`INCR`이 원자적이라 동시 요청에도 카운트가 정확하다. 키 식별자는:
- `MEMBER`: 로그인 사용자의 memberId(`SecurityContext`에서).
- `IP`: `X-Forwarded-For`(프록시 뒤 실제 IP) 우선, 없으면 `RemoteAddr`.

> 면접 포인트: 이 방식은 **고정 윈도우**라 경계에서 버스트가 가능하다(윈도우 끝~다음 시작에 몰리면 2배 통과). 더 엄밀하겐 슬라이딩 윈도우(Sorted Set)나 토큰 버킷이 있다. 학습/포폴 수준엔 고정 윈도우로 충분.

---

## 5. Redis 장애 시 (운영 관점)

현재는 단일 인스턴스라 Redis가 죽으면 선점·대기열·쿠폰·로그인이 마비된다. [ARCHITECTURE.md](ARCHITECTURE.md)의 대응:
- HA: Sentinel / Cluster / ElastiCache Multi-AZ.
- Circuit Breaker(Resilience4j)로 Redis 다운 시 degraded mode / DB fallback.

포폴은 단일로 두되 "다중화 가능한 구조"로 설계했다는 게 답변 포인트.

---

## 6. 면접 한 줄 정리

> 동시성·핫스팟·수명 있는 데이터(좌석 선점·대기열·쿠폰 재고·리프레시 토큰·Rate Limit)는 전부 Redis로 분리하고, DB엔 결제 확정 같은 영구·정합성 데이터만 남겼습니다. 단순 원자 명령은 `StringRedisTemplate`, 다중 서버 조율은 Redisson 분산락으로 나눴고, Rate Limiting은 `INCR` + TTL 고정 윈도우를 AOP 어노테이션으로 한 줄에 적용되게 만들었습니다.

---

> 다음 문서: **[12. 이력 · 통계 · 배치 →](12_이력_통계_배치.md)**
