package com.ticketing;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate; // 👈 추가

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")   // 공통 테스트 설정(application-test.properties) — ticketing_test DB
// ⚠️ 클래스 레벨의 @Transactional은 제거된 상태를 유지하여 OOM을 방지합니다.
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BulkInsertReservationTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    TransactionTemplate txTemplate; // 👈 프로그래밍 방식의 트랜잭션 제어를 위해 주입

    private Long memberId;
    private Long scheduleId;

    @BeforeEach
    void setup() {
        // 🛠️ TransactionTemplate을 사용하여 셋업 로직을 실제 트랜잭션 안에서 실행합니다.
        txTemplate.execute(status -> {
            Seller seller = Seller.create("bench-seller@t.com", "pw", "벤치셀러", "010-0000-0000",
                    new Address("서울", "벤치로", "00000"), "벤치컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("bench@t.com", "pw", "벤치회원", "벤치닉",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001", new Address("서울", "벤치로", "00000"));
            em.persist(member);

            Venue venue = Venue.create("벤치홀", new Address("서울", "벤치로", "00000"), 1, 1);
            em.persist(venue);

            Event event = Event.create("벤치공연", "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(schedule);

            em.flush();

            memberId = member.getId();
            scheduleId = schedule.getId();
            return null;
        });
    }

    // @AfterEach 정리 불필요 — @DirtiesContext(BEFORE_EACH)가 매 테스트 전 컨텍스트+스키마를 재생성한다.

    @Test
    void bench_100() {
        bench(100);
    }

    @Test
    void bench_1_000() {
        bench(1_000);
    }

    @Test
    void bench_10_000() {
        bench(10_000);
    }

    private void bench(int n) {
        long jpaMs = time(() -> insertJpa(n));
        long jdbcMs = time(() -> insertJdbc(n));

        double ratio = jdbcMs == 0 ? 0 : (double) jpaMs / jdbcMs;
        System.out.printf("%n=== [%,d건] JPA saveAll = %,d ms / JDBC batch = %,d ms → JDBC가 약 %.1f배 빠름 ===%n",
                n, jpaMs, jdbcMs, ratio);
    }

    // JPA: 1000건 단위 "청크 트랜잭션" + flush/clear.
    //  → JDBC Batch(청크 단위)와 트랜잭션 횟수를 맞춰 "공정하게" 비교한다.
    //    (이전엔 건별 트랜잭션이라 트랜잭션 오버헤드가 JPA에 불리하게 얹혔음)
    //
    //  ※ 그래도 JDBC Batch가 빠른 근본 이유:
    //    Reservation PK가 IDENTITY(AUTO_INCREMENT)라 INSERT를 실행해야 생성 PK를 받을 수 있어
    //    Hibernate가 batch로 못 묶고 "건별 INSERT"가 나간다. (hibernate.jdbc.batch_size 켜도 IDENTITY면 무효)
    //    → JPA는 하나씩 왕복 = 대량 적재에 느림. SEQUENCE 전략이면 JPA도 batch 가능하나 MySQL은 IDENTITY.
    private void insertJpa(int n) {
        int chunk = 1000;
        for (int s = 0; s < n; s += chunk) {
            final int start = s;
            final int end = Math.min(s + chunk, n);
            txTemplate.execute(status -> {
                Member m = em.getReference(NormalMember.class, memberId);
                EventSchedule sch = em.getReference(EventSchedule.class, scheduleId);
                for (int i = start; i < end; i++) {
                    // ★ IDENTITY라 persist마다 "즉시" INSERT가 나간다.
                    //    PK(AUTO_INCREMENT)를 받아야 영속성 컨텍스트가 식별·관리하므로
                    //    쓰기지연 SQL 저장소에 담지 못함 → batch로 못 묶임 → 건별 왕복.
                    em.persist(Reservation.create(m, sch, "jpa-" + i, 10000));
                }
                em.flush();   // 쓰기지연분 DB 동기화 (단 IDENTITY INSERT는 위 persist에서 이미 나감)
                em.clear();   // 1차 캐시 비우기 (메모리 해제)
                return null;
            });
        }
    }

    // JDBC Batch 쪼개기 기법 적용 (OOM 방지)
    private void insertJdbc(int n) {
        LocalDateTime now = LocalDateTime.now();
        int batchSize = 5000;
        List<Object[]> args = new ArrayList<>(batchSize);

        for (int i = 0; i < n; i++) {
            args.add(new Object[]{memberId, scheduleId, "jdbc-" + i, 10000, "PENDING", now, now, "bench"});

            if ((i + 1) % batchSize == 0) {
                executeBatch(args);
                args.clear();
            }
        }

        if (!args.isEmpty()) {
            executeBatch(args);
        }
    }

    private void executeBatch(List<Object[]> args) {
        jdbc.batchUpdate(
                "INSERT INTO reservation " +
                        "(member_id, schedule_id, idempotency_key, total_price, status, created_at, updated_at, created_by) " +
                        "VALUES (?,?,?,?,?,?,?,?)",
                args);
    }

    private long time(Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        return System.currentTimeMillis() - start;
    }
}