# 07. 좌석 선점 (Redis)

> **한 좌석에 여러 명이 동시에 몰릴 때, 어떻게 단 한 명만 잡게 하는가.**
> 이 프로젝트가 동시성을 다루는 가장 기본적인 장치다. DB 락 대신 Redis를 쓴 이유부터 본다.

---

## 1. 문제 상황

인기 공연 오픈 직후, A열 1번 좌석을 1000명이 동시에 클릭한다. 이 중 **정확히 한 명만** 성공해야 하고, 나머지 999명은 "이미 선택된 좌석"을 받아야 한다.

순진하게 짜면:
```
1) SELECT status FROM event_seat WHERE id=1   → AVAILABLE
2) (아 비었네)
3) UPDATE event_seat SET status=HELD WHERE id=1
```
1번과 3번 사이에 다른 사람이 끼어들면 **둘 다 AVAILABLE을 보고 둘 다 점유**한다(Race Condition).

---

## 2. 왜 DB가 아니라 Redis인가

DB로도 풀 수는 있다(비관적 락 `SELECT ... FOR UPDATE`, 낙관적 락 `@Version`). 하지만:

| | DB 락 | Redis |
|---|---|---|
| 핫스팟 | 인기 좌석 행에 락 경합 집중 → 커넥션 점유·대기 | 인메모리, 단일 스레드라 경합이 빠르게 정리 |
| 부하 위치 | 가장 비싼 자원(DB)에 동시성 부하 | DB 밖으로 분리 |
| 임시성 | 점유는 7분짜리 임시 데이터인데 DB row/락 유지 | TTL로 자동 소멸 |

→ CLAUDE.md §6.7: **"좌석 선점/대기열 같은 핫스팟은 DB 인덱스로 풀지 말고 Redis로 우회."** 점유는 임시 데이터라 Redis의 TTL과 궁합이 좋다.

---

## 3. 핵심: `SET NX` 한 줄의 원자성

[SeatHoldService.java](../src/main/java/com/ticketing/event/service/SeatHoldService.java)
```java
private static final Duration HOLD_TTL = Duration.ofMinutes(7);

private String key(Long scheduleId, Long eventSeatId) {
    return "seat:hold:" + scheduleId + ":" + eventSeatId;   // 회차+좌석 단위 키
}
```
```java
Boolean ok = redisTemplate.opsForValue()
    .setIfAbsent(key(scheduleId, seatId), memberId.toString(), HOLD_TTL);
```
`setIfAbsent` = Redis `SET key value NX EX 420`:
- **NX**: 키가 **없을 때만** 세팅. 이미 있으면 실패(`false`).
- **EX 420**: 7분 뒤 자동 삭제(TTL).
- 이 명령은 **원자적**이다. Redis는 단일 스레드로 명령을 처리하므로 "확인-후-세팅" 사이에 다른 명령이 못 끼어든다.

→ 1000명이 동시에 `SET NX`를 던져도 **맨 처음 도착한 하나만** `true`, 나머지는 전부 `false`. §1의 Race Condition이 원천 차단된다. 값으로 `memberId`를 넣어 "누가 잡았는지"도 안다.

---

## 4. 여러 좌석을 한 번에 — 전부 아니면 전무

좌석을 2~3개 한 번에 고르는 경우, 중간까지 잡다 하나가 막히면 **부분 점유**가 남으면 안 된다.
```java
public boolean holdAll(Long scheduleId, List<Long> eventSeatIds, Long memberId) {
    List<Long> heldByMe = new ArrayList<>();
    for (Long seatId : eventSeatIds) {
        Boolean ok = setIfAbsent(...);
        if (Boolean.TRUE.equals(ok)) heldByMe.add(seatId);   // 성공한 건 기록
        else {
            releaseAll(scheduleId, heldByMe);                 // 실패 → 지금까지 잡은 것 전부 반납
            return false;
        }
    }
    return true;
}
```
A·B·C 좌석을 잡는데 C에서 막히면 A·B를 즉시 반납하고 실패. → "전부 잡거나, 하나도 안 잡거나"(원자적 묶음에 준하는 보상 로직).

> 더 엄밀히는 Lua 스크립트로 멀티키를 진짜 원자적으로 처리할 수도 있다. 여기선 "성공분 즉시 보상" 방식으로 단순하게 갔다(좌석 수 최대 3개라 충분). 면접에서 "Lua로 더 강하게 묶을 수 있다"까지 말하면 좋다.

---

## 5. 점유 현황 보여주기 — 좌석맵의 HELD 표시

좌석 페이지는 매초 폴링하며 "어떤 좌석이 임시 점유 중인지"를 보여줘야 한다.
[EventSeatService.findByScheduleId()](../src/main/java/com/ticketing/event/service/EventSeatService.java)
```java
List<EventSeat> seats = eventSeatRepository.findByEventScheduleId(scheduleId);
Set<Long> heldIds = seatHoldService.findHeldSeatIds(scheduleId, seatIds);  // Redis 일괄 조회

return seats.stream().map(seat -> {
    EventSeatStatus status = seat.getStatus();          // DB 상태 (AVAILABLE/RESERVED)
    if (status == AVAILABLE && heldIds.contains(seat.getId()))
        status = HELD;                                   // Redis에 점유 키가 있으면 HELD로 덧칠
    return EventSeatResponseDto.from(seat, status);
}).toList();
```
[findHeldSeatIds](../src/main/java/com/ticketing/event/service/SeatHoldService.java)는 `multiGet`(MGET)으로 좌석 키들을 **한 번에** 조회한다(좌석마다 GET 날리면 N번 왕복 → 1번으로).

핵심 설계: **DB의 `status`는 `AVAILABLE/RESERVED`만, `HELD`는 Redis가 진실**이다. 응답 시점에 둘을 합쳐 보여준다. 그래서 임시 점유가 7분 만에 풀려도 DB를 건드릴 필요가 없다(TTL이 알아서).

---

## 6. 점유의 일생 (TTL과 상태 전이)

```
좌석 클릭 → 예매 생성
   holdAll: SET NX seat:hold:{sch}:{seat} = memberId  EX 420
   │
   ├─ 7분 안에 결제  → pay():
   │     isHeldByAll 확인 → DB status=RESERVED → releaseAll(키 삭제)
   │     이후 점유의 진실 = DB
   │
   └─ 7분 안에 결제 X → TTL 만료로 키 자동 소멸
         좌석은 다시 AVAILABLE로 보임 (DB는 손 안 댐)
```

결제 시 [PaymentService](../src/main/java/com/ticketing/payment/service/PaymentService.java)의 검증:
```java
public boolean isHeldByAll(Long scheduleId, List<Long> eventSeatIds, Long memberId) {
    List<String> values = redisTemplate.opsForValue().multiGet(keys);
    String me = memberId.toString();
    return values.size() == eventSeatIds.size() && values.stream().allMatch(me::equals);
}
```
"이 좌석들이 **전부 아직 내 것인가**"를 확인. 하나라도 만료(null)되거나 남의 것이면 `false` → `SEAT_HOLD_EXPIRED`. 만료 후 누가 채갔으면 결제를 막는다.

---

## 7. 분산락은 어디 쓰나 (Redisson)

좌석 선점은 `SET NX`라 별도 락이 필요 없다(키 자체가 락 역할). Redisson 분산락(`RLock`)은 **대기열 승급 스케줄러**에서 쓴다 — 여러 서버 인스턴스가 동시에 같은 작업을 돌리지 않게. → [08 대기열 §승급](08_대기열.md), [11 Redis 총정리](11_Redis_총정리_RateLimit.md).

[RedissonConfig](../src/main/java/com/ticketing/config/RedissonConfig.java)는 단일 서버 모드로 잡혀 있다(`useSingleServer`). 운영에선 Sentinel/Cluster로 교체 — [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 8. 면접 한 줄 정리

> 좌석 동시성은 Redis `SET NX`(키 없을 때만 세팅 + TTL)의 원자성으로 풀었습니다. DB 락을 쓰면 가장 비싼 자원에 핫스팟이 몰리지만, 점유는 7분짜리 임시 데이터라 Redis TTL과 궁합이 좋고 DB 부하를 분리할 수 있습니다. 결제 시점에 점유 주체를 Redis에서 DB(`RESERVED`)로 넘기고 점유 키를 해제합니다.

---

> 다음 문서: **[08. 대기열 →](08_대기열.md)**
