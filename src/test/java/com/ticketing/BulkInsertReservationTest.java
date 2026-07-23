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
import org.junit.jupiter.api.Tag;
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

@Tag("local")
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BulkInsertReservationTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    TransactionTemplate txTemplate;

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


    private void insertJpa(int n) {
        int chunk = 1000;
        for (int s = 0; s < n; s += chunk) {
            final int start = s;
            final int end = Math.min(s + chunk, n);
            txTemplate.execute(status -> {
                Member m = em.getReference(NormalMember.class, memberId);
                EventSchedule sch = em.getReference(EventSchedule.class, scheduleId);
                for (int i = start; i < end; i++) {

                    em.persist(Reservation.create(m, sch, "jpa-" + i, 10000));
                }
                em.flush();
                em.clear();
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