# 13. 성능 before / after

> 수십만 건 데이터를 깔아놓고 **N+1·인덱스·대량 INSERT**가 실제로 얼마나 차이 나는지 측정하기 위한 판을 어떻게 깔았는지,
> 그리고 어디를 어떻게 개선할 수 있는지 정리한다. "측정 후 최적화" 원칙(CLAUDE.md §6.7, §9)의 실천 문서다.

> ⚠️ 이 프로젝트는 **측정 환경을 의도적으로 만들어 둔** 단계다. N+1을 일부러 살려둔(코드 곳곳의 `// TODO N+1`) 이유는 "before"를 직접 보기 위해서다. (캐시는 측정용이 아니라 **효용이 낮아 미도입** — [05 §7](05_공연_좌석.md))

---

## 1. 측정용 대량 시드 — 판 깔기

`local` 프로파일에서만 도는 [DataSeedRunner](../src/main/java/com/ticketing/config/DataSeedRunner.java)가 측정용 데이터를 깐다.
```java
private static final int PERF_MEMBERS = 10000;       // 회원 1만
private static final int PERF_RESERVATIONS = 500000; // 예매 50만
private static final int REVIEWS_PER_EVENT = 150;    // 공연당 리뷰 150
```
- 회원 1만, 예매 **50만**, 공연 ~90개 + 회차·좌석 풀, 공연별 리뷰 150 → 인덱스/N+1 차이가 극명하게 드러나는 규모.
- 멱등성: `admin@test.com`이 이미 있으면 통째로 건너뛴다(재실행 안전).

### 시드 자체가 성능 기법의 시연
대량 적재를 그냥 JPA `save`로 하면 느리고 메모리가 터진다. 그래서:

**① JPA 부분 — 1000건마다 flush/clear**
```java
for (int i = 0; i < PERF_MEMBERS; i++) {
    normalMemberRepository.save(m);
    if (i % 1000 == 0) { em.flush(); em.clear(); }   // 영속성 컨텍스트 비워 메모리 관리
}
```
- 비번 인코딩(`bcrypt`)은 **한 번만** 하고 재사용 — 1만 번 bcrypt면 그것만으로 수십 초.

**② 예매·결제 50만 건 — JDBC batch + 청크 커밋**
[seedPerfReservationsJdbc()](../src/main/java/com/ticketing/config/DataSeedRunner.java)
```java
int chunkSize = 10000;
for (int start = 0; start < PERF_RESERVATIONS; start += chunkSize) {
    // 1만 건씩 Object[] 모아서
    tx.executeWithoutResult(status -> {                       // 청크마다 별도 트랜잭션 커밋
        jdbcTemplate.batchUpdate("INSERT INTO reservation ...", reservationArgs);
        jdbcTemplate.batchUpdate("INSERT INTO reservation_seat ...", seatArgs);
        jdbcTemplate.batchUpdate("INSERT INTO payment ...", paymentArgs);
    });
}
```
왜 JDBC batch인가:
- **IDENTITY 전략은 JPA batch INSERT가 안 묶인다**(INSERT 해야 PK를 알 수 있어 매 건 즉시 INSERT). → 대량엔 JPA가 비효율.
- 단일 트랜잭션으로 50만을 쌓으면 미커밋 undo log가 누적돼 **메모리 폭발**. → 1만 건 청크마다 커밋해 회피.
- `rewriteBatchedStatements=true`(JDBC URL)로 여러 INSERT를 한 네트워크 왕복으로 묶음.

---

## 2. JPA vs JDBC — 직접 측정 코드

[BulkInsertReservationTest](../src/test/java/com/ticketing/BulkInsertReservationTest.java)가 둘을 같은 조건에서 잰다.
```java
private void bench(int n) {
    long jpaMs  = time(() -> insertJpa(n));    // JPA saveAll (건별 트랜잭션 + 1000건마다 clear)
    long jdbcMs = time(() -> insertJdbc(n));   // JDBC batchUpdate (5000건 청크)
    System.out.printf("[%,d건] JPA = %,d ms / JDBC batch = %,d ms → JDBC가 약 %.1f배 빠름%n",
        n, jpaMs, jdbcMs, (double) jpaMs / jdbcMs);
}
```
- `@DirtiesContext(BEFORE_EACH_TEST_METHOD)` + `@AfterEach`의 `TRUNCATE`로 매 테스트 깨끗한 상태에서 측정.
- 결론(전형적): 대량 INSERT는 **JDBC batch가 JPA saveAll보다 수 배~수십 배 빠르다.** "대량 적재엔 JPA를 고집하지 말고 JDBC batch"라는 근거 데이터를 직접 만든 것.

> 면접 포인트: "왜 JPA가 느린가"에 IDENTITY batch 미지원 + 영속성 컨텍스트 관리 비용으로 답할 수 있다.

---

## 3. N+1 — 어디에 잠복해 있나

코드에 `// TODO N+1` 메모로 표시된 잠복 지점들(의도적으로 살려둠):

| 위치 | 증상 | 개선 |
|---|---|---|
| [EventService.search](../src/main/java/com/ticketing/event/service/EventService.java) | 공연 N개 → 회차/좌석 LAZY 접근마다 추가 쿼리 | fetch join 또는 `@BatchSize` |
| [ReservationService.findMine](../src/main/java/com/ticketing/reservation/service/ReservationService.java) | 예매 N건 → schedule/event/venue/seat 각각 LAZY | fetch join, DTO 프로젝션 |
| [EventSeatRepository.findByEventScheduleId](../src/main/java/com/ticketing/event/repository/EventSeatRepository.java) | 좌석 조회 후 seat 접근 | fetch join |
| [CouponService.findMyCoupon](../src/main/java/com/ticketing/coupon/service/CouponService.java) | 발급 N건 → coupon LAZY | fetch join |

### N+1이 뭔가 (1줄)
목록 1번 쿼리로 N건을 가져온 뒤, 각 건의 연관 엔티티를 LAZY로 건드릴 때마다 쿼리가 1번씩 더 나가 **1 + N번** 날아가는 문제. 예매 50만 건 시드에서 `findMine`을 페이징해 보면 SQL 로그(`p6spy`/`show-sql`)로 추가 쿼리가 폭증하는 걸 직접 볼 수 있다.

### 개선 방향 (after)
- **fetch join**: `select r from Reservation r join fetch r.eventSchedule.event ...` → 한 방에.
  - 단, 페이징 + 컬렉션 fetch join은 메모리 페이징 위험 → ToOne만 fetch join하거나 `@BatchSize`/`default_batch_fetch_size`로 IN 절 묶기.
- **DTO 프로젝션**: `Projections.constructor`로 필요한 컬럼만(이미 통계 집계에서 쓰는 기법 [12 §4](12_이력_통계_배치.md)).

---

## 4. 인덱스 — 측정하고 건다

CLAUDE.md §6.7: **조회 패턴 먼저, 인덱스 나중. 추측으로 만들지 말 것.** 50만 건이 깔려 있으니 `EXPLAIN`으로 before/after를 본다.

### 이미 걸린 제약성 인덱스 (UNIQUE)
- `reservation.idempotency_key` UNIQUE → 멱등성 조회([06](06_예매_결제.md))가 인덱스 탐색.
- `member.email`, `seller.business_number`, `review(member_id, event_id)` 등.

### 조회 패턴상 후보 (측정 후 도입)
| 쿼리 | 후보 인덱스 | 비고 |
|---|---|---|
| `findMine`: member_id + 기간 정렬 | `(member_id, created_at)` | 내 예매 페이징. created_at 정렬까지 커버 |
| 매출 집계: status + created_at 범위 | `payment(status, created_at)` | [12 §4](12_이력_통계_배치.md) 집계 |
| 좌석 조회: schedule_id | `event_seat(schedule_id)` | 좌석맵 |

**SOFT DELETE 컬럼 주의**: `deleted_at`을 인덱스 **선두에 두지 않는다**. 비즈니스 컬럼 뒤에 둔다(§6.7). 선두에 두면 카디널리티가 낮아 비효율.

> 핫스팟(좌석 선점·대기열)은 인덱스로 풀지 않는다 — Redis로 우회([07](07_좌석선점_Redis.md), [08](08_대기열.md)). 인덱스는 일반 조회용.

---

## 5. 측정 도구 — 무엇으로 보나

- `spring.jpa.show-sql=true` + `format_sql=true` → 쿼리 개수/모양 눈으로(N+1 적발).
- **p6spy** → 실행된 실제 SQL과 바인딩·실행시간 추적(ARCHITECTURE/기술스택에 명시).
- **Micrometer + Prometheus + Grafana** → 응답시간·처리량 메트릭(`/actuator/prometheus` 노출).
- `EXPLAIN` (MySQL) → 인덱스 타는지, 풀스캔인지.

---

## 6. 개선 순서 (측정 → 가벼운 것부터)

ARCHITECTURE.md "필요 시 단순한 것부터" 철학대로:
1. **N+1 제거**(fetch join / batch size) — 인프라 추가 0, 효과 큼.
2. **인덱스**(EXPLAIN으로 확인 후) — 조회 병목.
3. **캐시는 도입 보류** — 공연 목록이 소량이라 효용 대비 정합성 비용이 커서 미도입([05 §7](05_공연_좌석.md)). 데이터가 커지면 재검토.
4. **대량 쓰기**는 JDBC batch(이미 시드에 적용).
5. 그 이상(읽기 복제, 큐)은 트래픽이 실제로 요구할 때.

---

## 7. 면접 한 줄 정리

> 회원 1만·예매 50만 규모 시드를 깔아 N+1과 인덱스 효과를 실측할 수 있게 했습니다. 대량 적재는 IDENTITY의 JPA batch 미지원·메모리 누적 문제 때문에 JDBC batch + 청크 커밋으로 풀었고, 이를 테스트 코드로 JPA 대비 수 배 빠름을 직접 측정했습니다. N+1은 fetch join·배치 페치로, 인덱스는 EXPLAIN으로 검증 후 도입하며, 핫스팟은 인덱스가 아니라 Redis로 우회한다는 원칙을 지킵니다. "측정 후 최적화"가 기준입니다.

---

> 처음으로: **[00. 프로젝트 개요 →](00_프로젝트_개요.md)**
